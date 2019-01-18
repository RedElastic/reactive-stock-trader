package com.redelastic.stocktrader.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class OrderDetails {
    @NonNull String symbol;
    int shares;
    @NonNull OrderType orderType;
    @NonNull OrderConditions orderConditions;
}
