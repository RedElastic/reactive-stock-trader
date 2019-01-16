package com.redelastic.stocktrader.wiretransfer.api;

import lombok.NonNull;
import lombok.Value;

@Value
class PortfolioCreditRequest {
    @NonNull String portfolioId;

    @NonNull String accountId;
}
