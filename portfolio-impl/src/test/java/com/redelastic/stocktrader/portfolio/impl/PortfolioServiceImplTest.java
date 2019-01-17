package com.redelastic.stocktrader.portfolio.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import akka.stream.testkit.TestSubscriber;
import akka.stream.testkit.javadsl.TestSink;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.testkit.ProducerStub;
import com.lightbend.lagom.javadsl.testkit.ProducerStubFactory;
import com.redelastic.stocktrader.broker.api.*;
import com.redelastic.stocktrader.order.OrderConditions;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderType;
import com.redelastic.stocktrader.portfolio.api.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PortfolioServiceImplTest {

    private static TestServer server;

    @BeforeClass
    public static void setUp() {
        server = startServer(defaultSetup().withCassandra()
                .configureBuilder(b ->
                        b.overrides(bind(BrokerService.class).to(BrokerStub.class))
                ));
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private static ProducerStub<OrderResult> orderResultProducerStub;

    // Could consider mocking this per test, however this will require creating a new server per test (to resolve DI),
    // which will spin up C* each time and slow the tests down.
    static class BrokerStub implements BrokerService {

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
        public Topic<OrderResult> orderResults() {
            return orderResultProducerStub.topic();
        }
    }


    @Test
    public void placeOrder() throws Exception {
        PortfolioService service = server.client(PortfolioService.class);
        OpenPortfolioDetails details = OpenPortfolioDetails.builder().name("portfolioName").build();
        String portfolioId = service.openPortfolio().invoke(details).toCompletableFuture().get(5, SECONDS);
        Source<OrderPlaced, ?> source = service.orderPlaced().subscribe().atMostOnceSource();
        TestSubscriber.Probe<OrderPlaced> probe =
                source.runWith(TestSink.probe(server.system()), server.materializer());

        String symbol = "IBM";
        int shares = 31;
        OrderType orderType = OrderType.BUY;
        OrderConditions orderConditions = OrderConditions.Market.INSTANCE;
        OrderDetails orderDetails = OrderDetails.builder()
                .symbol(symbol)
                .shares(shares)
                .orderType(orderType)
                .orderConditions(orderConditions)
                .build();

        service.placeOrder(portfolioId).invoke(orderDetails).toCompletableFuture().get(5, SECONDS);

        OrderPlaced orderPlaced = probe.request(1).expectNext();
        assertEquals(orderDetails, orderPlaced.getOrderDetails());
        assertEquals(portfolioId, orderPlaced.getPortfolioId());

        BigDecimal sharePrice = BrokerStub.sharePrice;
        BigDecimal totalPrice = sharePrice.multiply(BigDecimal.valueOf(shares));
        OrderResult orderResult = OrderResult.OrderFulfilled.builder()
                .orderId(orderPlaced.getOrderId())
                .portfolioId(portfolioId)
                .trade(Trade.builder()
                    .symbol(symbol)
                        .shares(shares)
                        .orderType(orderType)
                        .price(sharePrice)
                        .build()
                )
                .build();
        orderResultProducerStub.send(orderResult);

        // Allow some time for the trade result to be processed by the portfolio
        eventually(FiniteDuration.create(10, SECONDS), () -> {
            PortfolioView view = service.getPortfolio(portfolioId).invoke().toCompletableFuture().get(5, SECONDS);
            assertEquals(1, view.getHoldings().size());
            assertTrue(view.getHoldings().contains(new ValuedHolding(symbol, shares, totalPrice)));
        });
    }


}