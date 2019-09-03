package com.redelastic.stocktrader.broker.impl.trade;

import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.portfolio.api.order.Order;

import java.util.concurrent.CompletionStage;

public interface TradeService {
    CompletionStage<OrderResult> placeOrder(Order order);
}
