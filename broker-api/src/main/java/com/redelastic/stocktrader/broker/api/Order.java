package com.redelastic.stocktrader.broker.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Order {

    String symbol;

    int shares;

    OrderType type;
}
