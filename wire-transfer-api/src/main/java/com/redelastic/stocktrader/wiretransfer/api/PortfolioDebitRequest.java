package com.redelastic.stocktrader.wiretransfer.api;

import lombok.NonNull;
import lombok.Value;

@Value
class PortfolioDebitRequest {
    @NonNull String source;
    @NonNull String portfolioId;
}
