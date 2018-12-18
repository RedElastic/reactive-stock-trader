package com.redelastic.stocktrader.portfolio.api;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Holding {
    String symbol;

    int shares;

    BigDecimal currentValue;
}
