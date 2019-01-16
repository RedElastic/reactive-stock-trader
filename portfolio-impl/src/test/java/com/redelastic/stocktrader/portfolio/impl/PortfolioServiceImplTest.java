package com.redelastic.stocktrader.portfolio.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.broker.api.Quote;
import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import org.junit.Test;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;


public class PortfolioServiceImplTest {

    static class BrokerStub implements BrokerService {

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
            return null;
        }
    }



    @Test
    public void openPortfolio() {

        ServiceTest.Setup setup = defaultSetup()
                .withCassandra()
                .configureBuilder(b ->
                        b.overrides(bind(BrokerService.class).to(BrokerStub.class))
                );
        withServer(setup, server -> {
            PortfolioService service = server.client(PortfolioService.class);
            PersistentEntityRegistry reg = server.injector().instanceOf(PersistentEntityRegistry.class);
            OpenPortfolioDetails details = OpenPortfolioDetails.builder().name("portfolioName").build();
            String portfolioId = service.openPortfolio().invoke(details).toCompletableFuture().get(5, SECONDS);
            assertTrue(true);
        });
    }

    @Test
    public void placeOrder() {
    }
}