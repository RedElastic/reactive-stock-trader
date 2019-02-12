/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.portfolio.api.order;

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

    public abstract <T> T visit(Visitor<T> visitor);

    public interface Visitor<T> {
        T visit(Market m);

        T visit(Limit l);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Market extends OrderType {
        public static Market INSTANCE = new Market();

        private Market() {}

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Limit extends OrderType {
        @NonNull BigDecimal limitPrice;

        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}
