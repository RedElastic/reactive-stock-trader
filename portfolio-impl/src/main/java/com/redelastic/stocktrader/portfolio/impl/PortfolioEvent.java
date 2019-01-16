package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.order.OrderDetails;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Events should be fairly basic and general, as we'd like the events to remain stable over time. Commands may change
 * more easily.
 */

interface PortfolioEvent extends Jsonable, AggregateEvent<PortfolioEvent> {

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
        @NonNull String portfolioId;
        String name; /* FIXME: Would like this to be @NonNull, but I have some events with nulls
                      * in my journal. Tried to fix it with JsonMigrations, but these seem to be broken (or don't apply to event streams?)
                      * and don't work in dev mode (but seem to work with `sbt runAll`).
                      */
    }

    @Value
    class LiquidationStarted implements PortfolioEvent {
        @NonNull String portfolioId;
    }

    @Value
    class Closed implements PortfolioEvent {
        @NonNull String portfolioId;
    }

    @Value
    @Builder
    class SharesCredited implements PortfolioEvent {
        @NonNull String portfolioId;
        @NonNull String symbol;
        int shares;
    }

    @Value
    class SharesDebited implements PortfolioEvent {
        @NonNull String portfolioId;
        @NonNull String symbol;
        int shares;
    }

    @Value
    class FundsDebited implements PortfolioEvent {
        @NonNull String portfolioId;
        @NonNull BigDecimal amount;
    }

    @Value
    class FundsCredited implements PortfolioEvent {
        @NonNull String portfolioId;
        @NonNull BigDecimal amount;
    }


    @Value
    class OrderPlaced implements PortfolioEvent {
        @NonNull String orderId;
        @NonNull String portfolioId;
        @NonNull OrderDetails orderDetails;

        com.redelastic.stocktrader.portfolio.api.OrderPlaced asDomainEvent() {
            return com.redelastic.stocktrader.portfolio.api.OrderPlaced.builder()
                   .portfolioId(portfolioId)
                    .orderId(orderId)
                   .orderDetails(orderDetails)
                    .build();
        }
    }

}
