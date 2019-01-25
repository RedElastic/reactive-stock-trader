package com.redelastic.stocktrader.broker.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

/**
 * Domain event representing the completion of an order, either successfully as a fulfillment of the order, or
 * unsuccessfully.
 */
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

        <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class OrderFailed extends OrderResult {
        @NonNull String portfolioId;
        @NonNull String orderId;

        <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    interface Visitor<T> {
        T visit(OrderFulfilled orderFulfilled);
        T visit(OrderFailed orderFailed);
    }

    abstract <T> T visit(Visitor<T> visitor);
}
