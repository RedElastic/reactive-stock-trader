package com.redelastic.stocktrader.broker.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import lombok.*;

/**
 * Domain event representing the completion of an order, either successfully as a fulfillment of the order, or
 * unsuccessfully.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(OrderResult.Fulfilled.class),
        @JsonSubTypes.Type(OrderResult.Failed.class)
})
public abstract class OrderResult {
    private OrderResult() {}

    public abstract PortfolioId getPortfolioId();

    public abstract OrderId getOrderId();

    public abstract <T> T visit(Visitor<T> visitor);

    public interface Visitor<T> {
        T visit(Fulfilled orderFulfilled);

        T visit(Failed orderFailed);
    }

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    public static class Fulfilled extends OrderResult {
        @NonNull PortfolioId portfolioId;
        @NonNull OrderId orderId;
        @NonNull Trade trade;

        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class Failed extends OrderResult {
        @NonNull PortfolioId portfolioId;
        @NonNull OrderId orderId;

        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }
}
