package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import com.redelastic.stocktrader.order.Order;

import java.util.concurrent.CompletionStage;

public interface OrderFactory {
    CompletionStage<Done> placeOrder(Order order);
}
