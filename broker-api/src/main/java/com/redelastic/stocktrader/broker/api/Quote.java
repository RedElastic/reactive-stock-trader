package com.redelastic.stocktrader.broker.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Quote {
    @NonNull String symbol;
    @NonNull BigDecimal sharePrice;
}
