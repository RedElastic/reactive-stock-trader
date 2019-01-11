package com.redelastic.stocktrader.portfolio.api;

import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ValuedHolding {
    @NonNull String symbol;

    int shareCount;

    @NonNull BigDecimal marketValue;
}
