package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.order.Order;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Events should be fairly basic and general, as we'd like the events to remain stable over time. Commands may change
 * more easily.
 */

public interface PortfolioEvent extends Jsonable, AggregateEvent<PortfolioEvent> {

    int NUM_SHARDS = 20; // TODO: Determine the appropriate value

    AggregateEventShards<PortfolioEvent> TAG =
            AggregateEventTag.sharded(PortfolioEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventShards<PortfolioEvent> aggregateTag() {
        return TAG;
    }

    String getPortfolioId();

    @Value
    @Builder
    class Opened implements PortfolioEvent {
        String portfolioId;
        String linkedAccount;
        String name;
    }

    @Value
    class LiquidationStarted implements PortfolioEvent {
        String portfolioId;
    }

    @Value
    class Closed implements PortfolioEvent {
        String portfolioId;
    }

    @Value
    @Builder
    class SharesCredited implements PortfolioEvent {
        String portfolioId;
        String symbol;
        int shares;
    }

    @Value
    class SharesDebited implements PortfolioEvent {
        String portfolioId;
        String symbol;
        int shares;
    }

    @Value
    class FundsDebited implements PortfolioEvent {
        String portfolioId;
        BigDecimal amount;
    }

    @Value
    class FundsCredited implements PortfolioEvent {
        String portfolioId;
        BigDecimal amount;
    }


    @Value
    class OrderPlaced implements PortfolioEvent {
        String portfolioId;
        Order order;
    }

}
