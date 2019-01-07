package com.redelastic.stocktrader.broker.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Sale {
    String portfolioId;
    String orderId;
    String symbol;
    int shares;
}
