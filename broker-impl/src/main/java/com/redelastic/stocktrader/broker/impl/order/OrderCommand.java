package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import lombok.Value;

import java.util.Optional;


interface OrderCommand {
    @Value
    class PlaceOrder implements OrderCommand, ReplyType<Order> {
        String portfolioId;
        OrderDetails orderDetails;
    }

    @Value
    class Complete implements OrderCommand, ReplyType<Done> {
        OrderResult orderResult;
    }

    enum GetStatus implements OrderCommand, ReplyType<Optional<OrderStatus>> {
        INSTANCE
    }
}
