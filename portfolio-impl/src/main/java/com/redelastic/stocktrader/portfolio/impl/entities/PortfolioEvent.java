package com.redelastic.stocktrader.portfolio.impl.entities;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;
import lombok.Builder;
import lombok.Value;

public interface PortfolioEvent extends Jsonable, AggregateEvent<PortfolioEvent> {

    int NUM_SHARDS = 20;

    AggregateEventShards<PortfolioEvent> TAG =
            AggregateEventTag.sharded(PortfolioEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventShards<PortfolioEvent> aggregateTag() {
        return TAG;
    }

    @Value
    @Builder
    class Opened implements PortfolioEvent {

        String linkedAccount;
        String description;
    }

    enum Closed implements PortfolioEvent {
        INSTANCE
    }

    @Value
    class SharesBought implements PortfolioEvent {

        String symbol;
        int shares;
    }

    @Value
    class SharesSold implements PortfolioEvent {

        String symbol;
        int shares;
    }

    @Value
    class SharesTransferToBrokerForSale implements PortfolioEvent {

        String symbol;
        int shares;
    }

}
