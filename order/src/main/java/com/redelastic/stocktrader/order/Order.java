package com.redelastic.stocktrader.order;

import lombok.NonNull;
import lombok.Value;
import lombok.Builder;
import lombok.experimental.Wither;

@Value
@Builder
@Wither
public class Order {
    String orderId; // FIXME: This is currently nullable for the gateway to send a order and let the portfolio give it an ID.
    @NonNull String portfolioId;
    @NonNull String symbol;
    int shares;
    @NonNull OrderType orderType;
    OrderConditions conditions;
}
