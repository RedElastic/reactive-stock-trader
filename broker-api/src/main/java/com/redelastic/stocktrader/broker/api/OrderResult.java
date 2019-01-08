package com.redelastic.stocktrader.broker.api;

import lombok.Builder;
import lombok.Value;

public interface OrderResult {

    String getPortfolioId();
    String getOrderId();

    @Value
    @Builder
    public class OrderCompleted implements OrderResult {
        String portfolioId;
        String orderId;
        Trade trade;
    }

    @Value
    class OrderFailed implements OrderResult {
        String portfolioId;
        String orderId;
    }
}
