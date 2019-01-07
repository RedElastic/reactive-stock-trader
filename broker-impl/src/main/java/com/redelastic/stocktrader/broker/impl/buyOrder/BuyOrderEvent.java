package com.redelastic.stocktrader.broker.impl.buyOrder;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.order.Order;
import lombok.Value;

public interface BuyOrderEvent extends Jsonable, AggregateEvent<BuyOrderEvent> {

    int NUM_SHARDS = 4;
    AggregateEventShards<BuyOrderEvent> TAG = AggregateEventTag.sharded(BuyOrderEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventTagger<BuyOrderEvent> aggregateTag() {
        return TAG;
    }

    @Value
    class Ready implements BuyOrderEvent {
        String orderId;
        Order order;
    }

    @Value
    class Fulfilled implements BuyOrderEvent {
        String orderId;
        Order order;
    }

}
