package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Optional;


abstract class OrderCommand implements Jsonable {
    private OrderCommand() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    static final class PlaceOrder extends OrderCommand implements ReplyType<Order> {
        PortfolioId portfolioId;
        OrderDetails orderDetails;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    static class CompleteOrder extends OrderCommand implements ReplyType<Done> {
        OrderResult orderResult;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    static class GetStatus extends OrderCommand implements ReplyType<Optional<OrderStatus>> {
        private GetStatus() {}
        public static GetStatus INSTANCE = new GetStatus();
    }
}
