package com.redelastic.stocktrader.broker.api;

import lombok.Value;

@Value
public class Order {

    String symbol;

    int shares;
}
