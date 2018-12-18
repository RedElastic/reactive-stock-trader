package com.redelastic.stocktrader.broker.api;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Quote {
    String symbol;

    BigDecimal sharePrice;
}
