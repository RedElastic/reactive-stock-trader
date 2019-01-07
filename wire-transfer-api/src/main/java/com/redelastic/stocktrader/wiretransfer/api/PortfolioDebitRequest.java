package com.redelastic.stocktrader.wiretransfer.api;

import lombok.Value;

@Value
public class PortfolioDebitRequest {
    Account source;
    String portfolioId;
}
