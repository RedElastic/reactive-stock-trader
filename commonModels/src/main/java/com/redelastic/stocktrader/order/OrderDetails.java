package com.redelastic.stocktrader.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class OrderDetails {
    @NonNull String symbol;
    int shares;
    @NonNull OrderType orderType;
    @NonNull OrderConditions orderConditions;

    @JsonCreator
    OrderDetails(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("shares") int shares,
            @JsonProperty("orderType") OrderType orderType,
            @JsonProperty("orderConditions") OrderConditions orderConditions
    ) {
        this.symbol = symbol;
        this.shares = shares;
        this.orderType = orderType;
        this.orderConditions = orderConditions;
    }
}
