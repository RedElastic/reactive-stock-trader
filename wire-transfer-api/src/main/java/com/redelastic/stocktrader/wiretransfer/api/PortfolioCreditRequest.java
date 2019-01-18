package com.redelastic.stocktrader.wiretransfer.api;

import lombok.NonNull;
import lombok.Value;

@Value
public class PortfolioCreditRequest {
    @NonNull String transferId;
    @NonNull String portfolioId;
    @NonNull String accountId;
}
