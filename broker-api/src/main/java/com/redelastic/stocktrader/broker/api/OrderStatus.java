package com.redelastic.stocktrader.broker.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(OrderStatus.Pending.class),
        @JsonSubTypes.Type(OrderStatus.Fulfilled.class),
        @JsonSubTypes.Type(OrderStatus.Failed.class)
})
public abstract class OrderStatus {
    private OrderStatus() {}

    public static class Pending extends OrderStatus {
        private Pending() {}
        public static Pending INSTANCE = new Pending();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Fulfilled extends OrderStatus {
        @NonNull BigDecimal price;
    }

    public static class Failed extends OrderStatus {
        private Failed() {}
        public static Failed INSTANCE = new Failed();
    }

}
