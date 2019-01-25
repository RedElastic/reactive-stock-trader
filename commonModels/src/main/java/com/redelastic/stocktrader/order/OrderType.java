package com.redelastic.stocktrader.order;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(OrderType.Market.class),
        @JsonSubTypes.Type(OrderType.Limit.class)
})
public abstract class OrderType {
    private OrderType() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Market extends OrderType {
        private Market() {}
        public static Market INSTANCE = new Market();

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Limit extends OrderType {
        @NonNull BigDecimal price;

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public interface Visitor<T> {
        T visit(Market m);
        T visit(Limit l);
    }

    public abstract  <T> T visit(Visitor<T> visitor);
}
