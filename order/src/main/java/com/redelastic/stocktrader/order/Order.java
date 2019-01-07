package com.redelastic.stocktrader.order;

import lombok.Value;
import lombok.Builder;
import lombok.experimental.Wither;

@Value
@Builder
@Wither
public class Order {
    String orderId;
    String portfolioId;
    String symbol;
    int shares;
    OrderType orderType;
    OrderConditions conditions;
}
