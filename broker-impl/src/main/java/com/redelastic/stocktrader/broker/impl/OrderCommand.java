package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.order.Order;
import lombok.Value;


interface OrderCommand {
    @Value
    class PlaceOrder implements OrderCommand, ReplyType<Done> {

        Order order;
    }

    @Value
    class Complete implements OrderCommand, ReplyType<Done> {
        OrderResult orderResult;
    }
}
