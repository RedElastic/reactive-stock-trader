package com.redelastic.stocktrader.broker.impl.sellOrder;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.redelastic.stocktrader.order.Order;
import lombok.Builder;
import lombok.Value;

public interface SellOrderEvent extends AggregateEvent<SellOrderEvent> {
    int NUM_SHARDS = 4;
    AggregateEventShards<SellOrderEvent> TAG = AggregateEventTag.sharded(SellOrderEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventTagger<SellOrderEvent> aggregateTag() {
        return TAG;
    }

    @Value
    @Builder
    final class Fulfilled implements SellOrderEvent {
        String orderId;
        Order order;
    }
}
