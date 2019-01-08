package com.redelastic.stocktrader.order;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(OrderConditions.Market.class),
        @JsonSubTypes.Type(OrderConditions.Limit.class)
})
public interface OrderConditions {

    @Value
    class Market implements OrderConditions {
        private Market() {}
        public static Market INSTANCE = new Market();
    }

    @Value
    class Limit implements OrderConditions {
        BigDecimal price;
    }
}
