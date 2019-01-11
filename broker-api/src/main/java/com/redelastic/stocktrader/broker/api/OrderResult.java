package com.redelastic.stocktrader.broker.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.NonNull;
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
    class OrderCompleted implements OrderResult {
        @NonNull String portfolioId;
        @NonNull String orderId;
        @NonNull Trade trade;
    }

    @Value
    class OrderFailed implements OrderResult {
        @NonNull String portfolioId;
        @NonNull String orderId;
    }
}
