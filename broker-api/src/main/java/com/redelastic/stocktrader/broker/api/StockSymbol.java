package com.redelastic.stocktrader.broker.api;

import lombok.NonNull;
import lombok.Value;
import lombok.Builder;

@Value
@Builder
public class StockSymbol {
    @NonNull String symbol;
}