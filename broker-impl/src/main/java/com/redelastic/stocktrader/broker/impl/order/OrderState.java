package com.redelastic.stocktrader.broker.impl.order;

import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import lombok.Value;

import java.math.BigDecimal;

public interface OrderState {

    OrderDetails getOrderDetails();
    OrderStatus getStatus();

    @Value
    class Pending implements OrderState {
        OrderDetails orderDetails;
        public OrderStatus getStatus() { return OrderStatus.Pending; }
    }

    @Value
    class Fulfilled implements OrderState {
        OrderDetails orderDetails;
        BigDecimal price;
        public OrderStatus getStatus() { return OrderStatus.Fulfilled; }
    }

    @Value
    class Failed implements OrderState {
        OrderDetails orderDetails;
        public OrderStatus getStatus() { return OrderStatus.Failed; }
    }

}