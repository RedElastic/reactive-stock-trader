package com.redelastic.stocktrader.portfolio.api;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class ValuedHolding {
    String symbol;

    int shareCount;

    BigDecimal marketValue;
}
