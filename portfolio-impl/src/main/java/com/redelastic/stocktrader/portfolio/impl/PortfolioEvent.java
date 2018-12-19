package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;
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
    public class Initialized implements PortfolioEvent {
        PortfolioId portfolioId;
        String linkedAccount;
    }


}
