package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.Order;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

public interface PortfolioCommand extends Jsonable {

    @Value
    class Open implements PortfolioCommand, ReplyType<Done> {
        @NonNull String name;
    }

    enum Liquidate implements PortfolioCommand, ReplyType<Done> {
        INSTANCE
    }

    enum GetState implements PortfolioCommand, ReplyType<PortfolioState.Open> {
        INSTANCE
    }

    @Value
    @Builder
    class PlaceOrder implements PortfolioCommand, ReplyType<Done> {
        @NonNull Order order;
    }

    @Value
    @Builder
    class CompleteTrade implements PortfolioCommand, ReplyType<Done> {
        @NonNull Trade trade;
    }

}
