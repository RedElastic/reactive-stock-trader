package com.redelastic.stocktrader.portfolio.impl;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.OrderConditions;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderType;
import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.Open;
import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.PlaceOrder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.*;

public class PortfolioModelEntityTest {

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

    private PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> createPortfolioEntity(String id) {
        return new PersistentEntityTestDriver<>(system, new PortfolioEntity(), id);
    }

    private PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> openPortfolioEntity(String id, String name) {
        PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> entity = createPortfolioEntity(id);
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
                .symbol(symbol)
                .shares(shareCount)
                .orderType(OrderType.BUY)
                .orderConditions(OrderConditions.Market.INSTANCE)
                .build();

        PersistentEntityTestDriver<PortfolioCommand,PortfolioEvent,Optional<PortfolioState>> driver = createPortfolioEntity(portfolioId);

        PersistentEntityTestDriver.Outcome<PortfolioEvent, Optional<PortfolioState>> outcome =
                driver.run(
                        new Open(pName),
                        new PlaceOrder(orderId, orderDetails));
        assertThat(outcome.state().get(), instanceOf(PortfolioState.Open.class));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.OrderPlaced(orderId, portfolioId, orderDetails)));
    }

    @Test
    public void moneyTransfers() {
        String portfolioId = "portfolioId";
        String pName = "portfolioName";
        PersistentEntityTestDriver<PortfolioCommand,PortfolioEvent,Optional<PortfolioState>> driver = openPortfolioEntity(portfolioId, pName);

        BigDecimal amount = new BigDecimal("123.45");
        BigDecimal difference = BigDecimal.valueOf(1);

        PortfolioCommand.ReceiveFunds transferIn = PortfolioCommand.ReceiveFunds.builder()
                .amount(amount)
                .build();

        PortfolioCommand.SendFunds transferOut = PortfolioCommand.SendFunds.builder()
                .amount(amount.subtract(difference))
                .build();

        PersistentEntityTestDriver.Outcome<PortfolioEvent,Optional<PortfolioState>> outcome = driver.run(
                transferIn,
                transferOut
        );
        assertThat(outcome.state().get(), instanceOf(PortfolioState.Open.class));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.FundsCredited(portfolioId, amount)));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.FundsDebited(portfolioId, amount.subtract(difference))));
        assertThat(difference, comparesEqualTo(((PortfolioState.Open)outcome.state().get()).getFunds()));
    }

    @Test
    public void denyOverSellingShares() {
        String portfolioId = "portfolioId";
        String portfolioName = "portfolio name";
        String symbol = "IBM";
        int shareCount = 10;
        String orderId = "orderId";
        BigDecimal price = new BigDecimal("1242.25");
        PersistentEntityTestDriver<PortfolioCommand,PortfolioEvent,Optional<PortfolioState>> driver = openPortfolioEntity(portfolioId, portfolioName);
        PersistentEntityTestDriver.Outcome<PortfolioEvent,Optional<PortfolioState>> outcome = driver.run(
                new PortfolioCommand.CompleteTrade(
                        Trade.builder()
                                .orderId(orderId)
                                .symbol(symbol)
                                .shares(shareCount)
                                .price(price)
                                .orderType(OrderType.BUY)
                                .build()
                ),
                new PortfolioCommand.PlaceOrder(orderId,
                        OrderDetails.builder()
                            .symbol(symbol)
                            .shares(shareCount+1)
                            .orderType(OrderType.SELL)
                            .orderConditions(OrderConditions.Market.INSTANCE)
                            .build()
                )
        );
        assertEquals(2, outcome.getReplies().size());
        assertEquals(outcome.getReplies().get(1).getClass(), InsufficientShares.class);
    }

}