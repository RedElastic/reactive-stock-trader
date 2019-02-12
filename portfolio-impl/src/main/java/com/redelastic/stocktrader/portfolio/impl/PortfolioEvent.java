/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.TransferId;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import lombok.AllArgsConstructor;
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

    PortfolioId getPortfolioId();

    @Value
    @Builder
    @AllArgsConstructor
    class Opened implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
        @NonNull String name;

    }

    @Value
    class LiquidationStarted implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
    }

    @Value
    class Closed implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
    }

    @Value
    @Builder
    class SharesCredited implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
        @NonNull String symbol;
        int shares;
    }

    @Value
    class SharesDebited implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
        @NonNull String symbol;
        int shares;
    }

    @Value
    class FundsDebited implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
        @NonNull BigDecimal amount;
    }

    @Value
    class FundsCredited implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
        @NonNull BigDecimal amount;
    }

    @Value
    class RefundAccepted implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
        @NonNull TransferId transferId;
        @NonNull BigDecimal amount;
    }


    @Value
    class OrderPlaced implements PortfolioEvent {
        @NonNull OrderId orderId;
        @NonNull PortfolioId portfolioId;
        @NonNull OrderDetails orderDetails;

        com.redelastic.stocktrader.portfolio.api.OrderPlaced asDomainEvent() {
            return com.redelastic.stocktrader.portfolio.api.OrderPlaced.builder()
                    .portfolioId(portfolioId)
                    .orderId(orderId)
                    .orderDetails(orderDetails)
                    .build();
        }

    }

    @Value
    class OrderFulfilled implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
        @NonNull OrderId orderId;
    }

    @Value
    class OrderFailed implements PortfolioEvent {
        @NonNull PortfolioId portfolioId;
        @NonNull OrderId orderId;
    }

}
