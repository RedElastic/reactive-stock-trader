package com.redelastic.stocktrader.portfolio.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import akka.stream.testkit.TestSubscriber;
import akka.stream.testkit.javadsl.TestSink;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.testkit.ProducerStub;
import com.lightbend.lagom.javadsl.testkit.ProducerStubFactory;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.broker.api.Quote;
import com.redelastic.stocktrader.order.OrderConditions;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderType;
import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import com.redelastic.stocktrader.portfolio.api.OrderPlaced;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Optional;

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

    static class BrokerStub implements BrokerService {

        @Inject
        BrokerStub(ProducerStubFactory producerFactory) {
            orderResultProducerStub = producerFactory.producer(ORDER_RESULTS_TOPIC_ID);
        }

        @Override
        public ServiceCall<NotUsed, Quote> getQuote(String symbol) {
            return null;
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
                source.runWith(
                        TestSink.probe(server.system()), server.materializer()
                );
        OrderDetails orderDetails = OrderDetails.builder()
                .symbol("IBM")
                .shares(31)
                .orderType(OrderType.BUY)
                .orderConditions(OrderConditions.Market.INSTANCE)
                .build();

        service.placeOrder(portfolioId).invoke(orderDetails)
                .thenAccept(done -> {
                    OrderPlaced orderPlaced = probe.request(1).expectNext();
                    assertEquals(orderDetails, orderPlaced.getOrderDetails());
                    assertEquals(portfolioId, orderPlaced.getPortfolioId());
                })
                .toCompletableFuture().get(5, SECONDS);
    }


}