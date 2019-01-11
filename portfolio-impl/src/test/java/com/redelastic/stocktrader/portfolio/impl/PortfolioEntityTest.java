package com.redelastic.stocktrader.portfolio.impl;

import static org.junit.Assert.*;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderConditions;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.*;

public class PortfolioEntityTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    private PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, PortfolioState> createPortfolioEntity(String id) {
        return new PersistentEntityTestDriver<>(system, new PortfolioEntity(), id);
    }

    @Test
    public void openAndPlaceOrder() {
        String portfolioId = "portfolioId";
        String pName = "portfolioName";
        String symbol = "IBM";
        String orderId = "orderId";
        int shareCount = 3;

        OrderDetails orderDetails = OrderDetails.builder()
                .portfolioId(portfolioId)
                .symbol(symbol)
                .shares(shareCount)
                .orderType(OrderType.BUY)
                .conditions(OrderConditions.Market.INSTANCE)
                .build();

        Order order = new Order(orderId, orderDetails);

        PersistentEntityTestDriver<PortfolioCommand,PortfolioEvent,PortfolioState> driver = createPortfolioEntity(portfolioId);

        PersistentEntityTestDriver.Outcome<PortfolioEvent, PortfolioState> outcome =
                driver.run(
                        new Open(pName),
                        new PlaceOrder(order));
        assertThat(outcome.state(), instanceOf(PortfolioState.Open.class));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.OrderPlaced(portfolioId, order)));
    }

    @Test
    public void moneyTransfers() {
        String portfolioId = "portfolioId";
        String pName = "portfolioName";
        String symbol = "IBM";
        String orderId = "orderId";
        int shareCount = 3;

        OrderDetails orderDetails = OrderDetails.builder()
                .portfolioId(portfolioId)
                .symbol(symbol)
                .shares(shareCount)
                .orderType(OrderType.BUY)
                .conditions(OrderConditions.Market.INSTANCE)
                .build();

        Order order = new Order(orderId, orderDetails);

        PersistentEntityTestDriver<PortfolioCommand,PortfolioEvent,PortfolioState> driver = createPortfolioEntity(portfolioId);

        PersistentEntityTestDriver.Outcome<PortfolioEvent, PortfolioState> outcome =
                driver.run(
                        new Open(pName));
        assertThat(outcome.state(), instanceOf(PortfolioState.Open.class));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.OrderPlaced(portfolioId, order)));
    }
}