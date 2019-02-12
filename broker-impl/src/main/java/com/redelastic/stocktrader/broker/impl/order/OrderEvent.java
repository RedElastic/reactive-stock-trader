/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.broker.impl.order;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.portfolio.api.order.Order;
import lombok.Value;

public interface OrderEvent extends Jsonable, AggregateEvent<OrderEvent> {

    int NUM_SHARDS = 20; // TODO: Determine the appropriate value
    AggregateEventShards<OrderEvent> TAG =
            AggregateEventTag.sharded(OrderEvent.class, NUM_SHARDS);

    Order getOrder();

    @Override
    default AggregateEventShards<OrderEvent> aggregateTag() {
        return TAG;
    }

    @Value
    class OrderReceived implements OrderEvent {
        Order order;
    }

    @Value
    class OrderFulfilled implements OrderEvent {
        Order order;
        Trade trade;
    }

    @Value
    class OrderFailed implements OrderEvent {
        Order order;
    }

}
