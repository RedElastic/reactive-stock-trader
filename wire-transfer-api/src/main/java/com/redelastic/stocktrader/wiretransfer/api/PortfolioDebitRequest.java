package com.redelastic.stocktrader.wiretransfer.api;

import lombok.NonNull;
import lombok.Value;

@Value
public class PortfolioDebitRequest {
    @NonNull String source;
    @NonNull String portfolioId;
}
