package com.redelastic.stocktrader.portfolio.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import akka.stream.testkit.TestSubscriber;
import akka.stream.testkit.javadsl.TestSink;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.testkit.ProducerStub;
import com.lightbend.lagom.javadsl.testkit.ProducerStubFactory;
import com.redelastic.stocktrader.TransferId;
import com.redelastic.stocktrader.broker.api.*;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderType;
import com.redelastic.stocktrader.order.TradeType;
import com.redelastic.stocktrader.portfolio.api.*;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import com.redelastic.stocktrader.wiretransfer.api.TransferRequest;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import lombok.extern.log4j.Log4j;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PSequence;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Log4j
public class PortfolioServiceImplTest {

    private static TestServer server;

    @BeforeClass
    public static void setUp() {
        server = startServer(defaultSetup().withCassandra()
                .configureBuilder(b ->
                    b.overrides(bind(BrokerService.class).to(BrokerStub.class))
                     .overrides(bind(WireTransferService.class).to(WireTransferStub.class))
                ));
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }




    // Could consider mocking this per test, however this will require creating a new server per test (to resolve DI),
    // which will spin up C* each time and slow the tests down.
    static class BrokerStub implements BrokerService {

        static ProducerStub<OrderResult> orderResultProducerStub;

        static BigDecimal sharePrice = new BigDecimal("152.12");

        @Inject
        BrokerStub(ProducerStubFactory producerFactory) {
            orderResultProducerStub = producerFactory.producer(ORDER_RESULTS_TOPIC_ID);
        }

        @Override
        public ServiceCall<NotUsed, Quote> getQuote(String symbol) {
            return notUsed -> CompletableFuture.completedFuture(
                    Quote.builder().symbol(symbol).sharePrice(sharePrice).build());
        }

        @Override
        public ServiceCall<NotUsed, Optional<OrderStatus>> getOrderStatus(String orderId) {
            return null;
        }

        @Override
        public Topic<OrderResult> orderResult() {

            return orderResultProducerStub.topic();
        }
    }

    static class WireTransferStub implements WireTransferService {

        static ProducerStub<TransferRequest> transferRequestProducerStub;
        static ProducerStub<Transfer> transferProducerStub;

        @Inject
        WireTransferStub(ProducerStubFactory producerStubFactory) {
            transferRequestProducerStub = producerStubFactory.producer(TRANSFER_REQUEST_TOPIC_ID);
            transferProducerStub = producerStubFactory.producer(PORTFOLIO_TRANSFER_TOPIC_ID);
        }

        @Override
        public ServiceCall<Transfer, TransferId> transferFunds() {
            return null;
        }

        @Override
        public Topic<Transfer> completedTransfers() {
            return transferProducerStub.topic();
        }

        @Override
        public Topic<TransferRequest> transferRequest() {
            return transferRequestProducerStub.topic();
        }
    }

    @Test
    public void placeBuyOrder() throws Exception {
        PortfolioService service = server.client(PortfolioService.class);
        OpenPortfolioDetails details = new OpenPortfolioDetails("portfolioName");
        val portfolioId = service.openPortfolio().invoke(details).toCompletableFuture().get(5, SECONDS);
        Source<OrderPlaced, ?> source = service.orderPlaced().subscribe().atMostOnceSource();
        TestSubscriber.Probe<OrderPlaced> probe =
                source.runWith(TestSink.probe(server.system()), server.materializer());

        String symbol = "IBM";
        int shares = 31;
        TradeType tradeType = TradeType.BUY;
        OrderType orderConditions = OrderType.Market.INSTANCE;
        OrderDetails orderDetails = OrderDetails.builder()
                .symbol(symbol)
                .shares(shares)
                .tradeType(tradeType)
                .orderType(orderConditions)
                .build();

        val orderId = service.placeOrder(portfolioId).invoke(orderDetails).toCompletableFuture().get(5, SECONDS);

        // Make sure we see the order published

        OrderPlaced orderPlaced = probe.request(1).expectNext();
        assertEquals(orderDetails, orderPlaced.getOrderDetails());
        assertEquals(portfolioId.getId(), orderPlaced.getPortfolioId());
        assertEquals(orderId.getId(), orderPlaced.getOrderId());



        BigDecimal sharePrice = BrokerStub.sharePrice;
        OrderResult orderResult = OrderResult.Fulfilled.builder()
                .orderId(orderId.getId())
                .portfolioId(portfolioId.getId())
                .trade(Trade.builder()
                        .orderId(orderId.getId())
                        .symbol(symbol)
                        .shares(shares)
                        .tradeType(tradeType)
                        .price(sharePrice)
                        .build()
                )
                .build();
        BrokerStub.orderResultProducerStub.send(orderResult);

        // Allow some time for the trade result to be processed by the portfolio
        eventually(FiniteDuration.create(10, SECONDS), () -> {
            PortfolioView view = service.getPortfolio(portfolioId).invoke().toCompletableFuture().get(5, SECONDS);
            assertEquals(1, view.getHoldings().size());
            assertTrue(view.getHoldings().contains(new Holding(symbol, shares)));
        });
    }

    @Test
    public void compensateForFailedSale() throws Exception {

        PortfolioService service = server.client(PortfolioService.class);
        OpenPortfolioDetails details = new OpenPortfolioDetails("portfolioName");
        val portfolioId = service.openPortfolio().invoke(details).toCompletableFuture().get(5, SECONDS);
        Source<OrderPlaced, ?> source = service.orderPlaced().subscribe().atMostOnceSource();
        TestSubscriber.Probe<OrderPlaced> probe =
                source.runWith(TestSink.probe(server.system()), server.materializer());

        // 1. Successfully purchase shares
        String symbol = "IBM";
        int sharesToBuy = 31;
        TradeType tradeType = TradeType.BUY;
        OrderType orderConditions = OrderType.Market.INSTANCE;
        OrderDetails buyOrderDetails = OrderDetails.builder()
                .symbol(symbol)
                .shares(sharesToBuy)
                .tradeType(tradeType)
                .orderType(orderConditions)
                .build();

        val buyOrderId = service.placeOrder(portfolioId).invoke(buyOrderDetails).toCompletableFuture().get(5, SECONDS);
        eventually(FiniteDuration.create(5, SECONDS), () -> {
            OrderPlaced orderPlaced = probe.request(1).expectNext();
            assertEquals(buyOrderDetails, orderPlaced.getOrderDetails());
            assertEquals(portfolioId.getId(), orderPlaced.getPortfolioId());
            assertEquals(buyOrderId.getId(), orderPlaced.getOrderId());
        });


        BigDecimal sharePrice = BrokerStub.sharePrice;
        OrderResult orderResult = OrderResult.Fulfilled.builder()
                .orderId(buyOrderId.getId())
                .portfolioId(portfolioId.getId())
                .trade(Trade.builder()
                        .orderId(buyOrderId.getId())
                        .symbol(symbol)
                        .shares(sharesToBuy)
                        .tradeType(tradeType)
                        .price(sharePrice)
                        .build()
                )
                .build();
        BrokerStub.orderResultProducerStub.send(orderResult);

        // Allow some time for the trade result to be processed by the portfolio
        eventually(FiniteDuration.create(10, SECONDS), () -> {
            PortfolioView view = service.getPortfolio(portfolioId).invoke().toCompletableFuture().get(5, SECONDS);
            assertEquals(1, view.getHoldings().size());
            assertTrue(view.getHoldings().contains(new Holding(symbol, sharesToBuy)));
        });

        int sharesToSell = 10;

        // 2. Unsuccessfully attempt to sell some of them
        OrderDetails sellOrderDetails = OrderDetails.builder()
                .tradeType(TradeType.SELL)
                .shares(sharesToSell)
                .symbol(symbol)
                .orderType(OrderType.Market.INSTANCE)
                .build();
        val sellOrderId = service
                .placeOrder(portfolioId)
                .invoke(sellOrderDetails)
                .toCompletableFuture()
                .get(5, SECONDS);

        OrderResult sellOrderResult = OrderResult.Failed.builder()
                .orderId(sellOrderId.getId())
                .portfolioId(portfolioId.getId())
                .build();

        PSequence<Holding> holdingsDuringSale = service.getPortfolio(portfolioId)
                .invoke()
                .toCompletableFuture()
                .get(5, SECONDS)
                .getHoldings();

        assertEquals(sharesToBuy-sharesToSell, holdingsDuringSale.get(0).getShareCount());

        BrokerStub.orderResultProducerStub.send(sellOrderResult);

        eventually(FiniteDuration.create(10, SECONDS), () -> {
            PortfolioView view = service.getPortfolio(portfolioId).invoke().toCompletableFuture().get(5, SECONDS);
            assertEquals(1, view.getHoldings().size());
            assertEquals(sharesToBuy, view.getHoldings().get(0).getShareCount());
        });
    }

    /*
     * Once we've processed a trade we should not reprocess it. The portfolio service consumes the trade results
     * topic using at-least-once handling to avoid missing a trade, so we need to ensure we handle duplicates.
     */
    @Test
    public void ignoreDuplicateTrades() throws Exception {
        PortfolioService service = server.client(PortfolioService.class);
        OpenPortfolioDetails details = new OpenPortfolioDetails("portfolioName");
        val portfolioId = service.openPortfolio().invoke(details).toCompletableFuture().get(5, SECONDS);
        Source<OrderPlaced, ?> source = service.orderPlaced().subscribe().atMostOnceSource();
        TestSubscriber.Probe<OrderPlaced> probe =
                source.runWith(TestSink.probe(server.system()), server.materializer());

        String symbol = "IBM";
        int sharesToBuy = 31;
        OrderDetails buyOrderDetails = OrderDetails.builder()
                .symbol(symbol)
                .shares(sharesToBuy)
                .tradeType(TradeType.BUY)
                .orderType(OrderType.Market.INSTANCE)
                .build();

        val orderId = service.placeOrder(portfolioId).invoke(buyOrderDetails).toCompletableFuture().get(5, SECONDS);

        BigDecimal price = new BigDecimal("123.45");
        OrderResult tradeResult = OrderResult.Fulfilled.builder()
                .orderId(orderId.getId())
                .portfolioId(portfolioId.getId())
                .trade(Trade.builder()
                        .orderId(orderId.getId())
                        .tradeType(TradeType.BUY)
                        .symbol(symbol)
                        .price(price)
                        .shares(sharesToBuy)
                        .build()
                ).build();

        BrokerStub.orderResultProducerStub.send(tradeResult);
        BrokerStub.orderResultProducerStub.send(tradeResult);


        PortfolioView view = service.getPortfolio(portfolioId).invoke().toCompletableFuture().get(5, SECONDS);
        assertEquals(1, view.getHoldings().size());
        assertEquals(sharesToBuy, view.getHoldings().get(0).getShareCount());

    }

}