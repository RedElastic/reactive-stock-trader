package com.redelastic.stocktrader.broker.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.redelastic.stocktrader.broker.api.Trade;
import lombok.Value;

interface OrderEvent extends AggregateEvent<OrderEvent> {
    String getOrderId();

    @Value
    class ProcessingOrder {
        String orderId;
    }

    @Value
    class OrderFulfilled {
        String orderId;
        Trade trade;
    }

}
