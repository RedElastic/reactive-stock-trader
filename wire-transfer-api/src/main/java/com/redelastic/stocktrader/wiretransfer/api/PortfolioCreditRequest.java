package com.redelastic.stocktrader.wiretransfer.api;

import lombok.Value;

@Value
public class PortfolioCreditRequest {
    String portfolioId;

    Account accountId;
}
