package com.redelastic.stocktrader.portfolio.impl;

import static org.junit.Assert.*;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderConditions;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertThat;

import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.*;

import java.math.BigDecimal;

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

    private PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, PortfolioState> openPortfolioEntity(String id, String name) {
        PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, PortfolioState> entity = createPortfolioEntity(id);
        entity.run(new PortfolioCommand.Open(name));
        return entity;
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

        Order order = Order.builder()
                .orderId(orderId)
                .details(orderDetails)
                .build();

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
        PersistentEntityTestDriver<PortfolioCommand,PortfolioEvent,PortfolioState> driver = openPortfolioEntity(portfolioId, pName);

        BigDecimal amount = new BigDecimal("123.45");
        BigDecimal difference = BigDecimal.valueOf(1);

        PortfolioCommand.ReceiveFunds transferIn = PortfolioCommand.ReceiveFunds.builder()
                .amount(amount)
                .build();

        PortfolioCommand.SendFunds transferOut = PortfolioCommand.SendFunds.builder()
                .amount(amount.subtract(difference))
                .build();

        PersistentEntityTestDriver.Outcome<PortfolioEvent,PortfolioState> outcome = driver.run(
                transferIn,
                transferOut
        );
        assertThat(outcome.state(), instanceOf(PortfolioState.Open.class));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.FundsCredited(portfolioId, amount)));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.FundsDebited(portfolioId, amount.subtract(difference))));
        assertThat(difference, comparesEqualTo(((PortfolioState.Open)outcome.state()).getFunds()));

    }

    @Test
    public void overSellShares() {
        String portfolioId = "portfolioId";
        String portfolioName = "portfolio name";
        String symbol = "IBM";
        int shareCount = 10;
        String orderId = "orderId";
        BigDecimal price = new BigDecimal("1242.25");
        PersistentEntityTestDriver<PortfolioCommand,PortfolioEvent,PortfolioState> driver = openPortfolioEntity(portfolioId, portfolioName);
        PersistentEntityTestDriver.Outcome<PortfolioEvent,PortfolioState> outcome = driver.run(
                new PortfolioCommand.CompleteTrade(
                        Trade.builder()
                                .symbol(symbol)
                                .shares(shareCount)
                                .price(price)
                                .orderType(OrderType.BUY)
                                .build()
                ),
                new PortfolioCommand.PlaceOrder(
                        Order.builder()
                            .orderId(orderId)
                            .details(
                                    OrderDetails.builder()
                                        .symbol(symbol)
                                        .shares(shareCount+1)
                                        .orderType(OrderType.SELL)
                                        .conditions(OrderConditions.Market.INSTANCE)
                                        .portfolioId(portfolioId)
                                        .build()
                            )
                            .build()
                )
        );
        assertEquals(2, outcome.getReplies().size());
        assertEquals(outcome.getReplies().get(1).getClass(), InsufficientShares.class);
    }
}