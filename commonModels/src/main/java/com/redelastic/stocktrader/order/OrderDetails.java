package com.redelastic.stocktrader.order;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class OrderDetails {
    @NonNull String symbol;
    int shares;
    @NonNull OrderType orderType;
    @NonNull OrderConditions conditions;
}
