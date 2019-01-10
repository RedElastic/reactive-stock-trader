package com.redelastic.stocktrader.order;

import lombok.NonNull;
import lombok.Value;
import lombok.Builder;
import lombok.experimental.Wither;

@Value
@Builder
@Wither
public class OrderDetails {
    @NonNull String portfolioId;
    @NonNull String symbol;
    int shares;
    @NonNull OrderType orderType;
    OrderConditions conditions;
}
