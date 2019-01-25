package com.redelastic.stocktrader.portfolio.api;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(OrderCompleted.Fulfilled.class),
        @JsonSubTypes.Type(OrderCompleted.Failed.class),
        @JsonSubTypes.Type(OrderCompleted.Expired.class),
        @JsonSubTypes.Type(OrderCompleted.Cancelled.class)
})
public abstract class OrderCompleted {
    private OrderCompleted() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Fulfilled extends OrderCompleted {
        @NonNull String portfolioId;
        @NonNull String orderId;
        @NonNull BigDecimal price;

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Failed extends OrderCompleted {
        @NonNull String portfolioId;
        @NonNull String orderId;

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Expired extends OrderCompleted {
        @NonNull String portfolioId;
        @NonNull String orderId;

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Cancelled extends OrderCompleted {
        @NonNull String portfolioId;
        @NonNull String orderId;

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public interface Visitor<T> {
        T visit(Fulfilled fulfilled);
        T visit(Failed failed);
        T visit(Expired expired);
        T visit(Cancelled cancelled);
    }

    public abstract  <T> T visit(Visitor<T> visitor);
}