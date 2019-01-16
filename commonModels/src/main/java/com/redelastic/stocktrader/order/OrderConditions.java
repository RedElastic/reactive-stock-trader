package com.redelastic.stocktrader.order;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(OrderConditions.Market.class),
        @JsonSubTypes.Type(OrderConditions.Limit.class)
})
public abstract class OrderConditions {
    private OrderConditions() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Market extends OrderConditions {
        private Market() {}
        public static Market INSTANCE = new Market();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Limit extends OrderConditions {
        @NonNull BigDecimal price;
    }
}
