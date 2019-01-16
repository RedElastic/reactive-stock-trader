package com.redelastic.stocktrader.broker.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(OrderResult.OrderFulfilled.class),
        @JsonSubTypes.Type(OrderResult.OrderFailed.class)
})
public abstract class OrderResult {
    private OrderResult() {}

    public abstract String getPortfolioId();
    public abstract String getOrderId();

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    public static class OrderFulfilled extends OrderResult {
        @NonNull String portfolioId;
        @NonNull String orderId;
        @NonNull Trade trade;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class OrderFailed extends OrderResult {
        @NonNull String portfolioId;
        @NonNull String orderId;
    }
}
