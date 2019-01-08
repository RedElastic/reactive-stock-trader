package com.redelastic.stocktrader.broker.impl;

import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.order.Order;

import java.util.concurrent.CompletionStage;

public interface TradeService {
    CompletionStage<OrderResult> placeOrder(Order order);
}
