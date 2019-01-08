package com.redelastic.stocktrader.broker.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(OrderResult.OrderCompleted.class),
        @JsonSubTypes.Type(OrderResult.OrderFailed.class)
})
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
