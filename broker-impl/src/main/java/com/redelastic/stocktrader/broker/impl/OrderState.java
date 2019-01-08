package com.redelastic.stocktrader.broker.impl;

import com.redelastic.stocktrader.order.Order;
import lombok.Value;

import java.math.BigDecimal;

public interface OrderState {

    Order getOrder();

    @Value
    class Pending implements OrderState {
        Order order;
    }

    @Value
    class Fulfilled implements OrderState {
        Order order;
        BigDecimal price;
    }

    @Value
    class Failed implements OrderState {
        Order order;
    }

}