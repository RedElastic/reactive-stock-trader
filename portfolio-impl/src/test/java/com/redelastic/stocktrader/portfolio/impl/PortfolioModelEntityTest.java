package com.redelastic.stocktrader.portfolio.impl;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.TradeType;
import com.redelastic.stocktrader.TransferId;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.order.OrderType;
import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.Open;
import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.PlaceOrder;
import lombok.val;
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

    private PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> openPortfolioEntity(PortfolioId portfolioId, String name) {
        PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> entity = createPortfolioEntity(portfolioId.getId());
        entity.run(new PortfolioCommand.Open(name));
        return entity;
    }

    @Test
    public void openAndPlaceOrder() {
        val portfolioId = new PortfolioId("portfolioId");
        String pName = "portfolioName";
        String symbol = "IBM";
        val orderId = new OrderId("orderId");
        int shareCount = 3;

        OrderDetails orderDetails = OrderDetails.builder()
                .symbol(symbol)
                .shares(shareCount)
                .tradeType(TradeType.BUY)
                .orderType(OrderType.Market.INSTANCE)
                .build();

        PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> driver = createPortfolioEntity(portfolioId.getId());

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
        val portfolioId = new PortfolioId("portfolioId");
        String pName = "portfolioName";
        PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> driver = openPortfolioEntity(portfolioId, pName);

        BigDecimal amount = new BigDecimal("123.45");
        BigDecimal difference = BigDecimal.valueOf(1);

        TransferId transferInId = TransferId.newId();

        PortfolioCommand.ReceiveFunds transferIn = new PortfolioCommand.ReceiveFunds(amount, transferInId);

        TransferId transferOutId = TransferId.newId();
        PortfolioCommand.SendFunds transferOut = new PortfolioCommand.SendFunds(amount.subtract(difference), transferOutId);

        PersistentEntityTestDriver.Outcome<PortfolioEvent, Optional<PortfolioState>> outcome = driver.run(
                transferIn,
                transferOut
        );
        assertThat(outcome.state().get(), instanceOf(PortfolioState.Open.class));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.TransferReceived(portfolioId, transferInId, amount)));
        assertTrue(outcome.events().contains(
                new PortfolioEvent.TransferSent(portfolioId, transferOutId, amount.subtract(difference))));
        assertThat(difference, comparesEqualTo(((PortfolioState.Open) outcome.state().get()).getFunds()));
    }

    @Test
    public void denyOverSellingShares() {
        val portfolioId = new PortfolioId("portfolioId");
        String portfolioName = "portfolio name";
        String symbol = "IBM";
        int shareCount = 10;
        val orderId = new OrderId("orderId");
        BigDecimal price = new BigDecimal("1242.25");
        PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> driver = openPortfolioEntity(portfolioId, portfolioName);
        PersistentEntityTestDriver.Outcome<PortfolioEvent, Optional<PortfolioState>> outcome = driver.run(
                new PortfolioCommand.CompleteTrade(
                        orderId,
                        Trade.builder()
                                .symbol(symbol)
                                .shares(shareCount)
                                .sharePrice(price)
                                .tradeType(TradeType.BUY)
                                .build()
                ),
                new PortfolioCommand.PlaceOrder(orderId,
                        OrderDetails.builder()
                                .symbol(symbol)
                                .shares(shareCount + 1)
                                .tradeType(TradeType.SELL)
                                .orderType(OrderType.Market.INSTANCE)
                                .build()
                )
        );
        assertEquals(2, outcome.getReplies().size());
        assertEquals(outcome.getReplies().get(1).getClass(), InsufficientShares.class);
    }

    @Test
    public void receiveFunds() {
        val portfolioId = new PortfolioId("portfolioId");
        String portfolioName = "portfolio name";
        BigDecimal amount = new BigDecimal("101.40");
        val transferId = TransferId.newId();
        PersistentEntityTestDriver<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> driver = openPortfolioEntity(portfolioId, portfolioName);
        PersistentEntityTestDriver.Outcome<PortfolioEvent, Optional<PortfolioState>> outcome = driver.run(
                new PortfolioCommand.ReceiveFunds(amount, transferId)
        );

        assertTrue(outcome.state().isPresent());
        assertThat(outcome.state().get(), instanceOf(PortfolioState.Open.class));
        assertEquals(amount, ((PortfolioState.Open) outcome.state().get()).getFunds());
    }

}