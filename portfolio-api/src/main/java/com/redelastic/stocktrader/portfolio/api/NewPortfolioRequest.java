package com.redelastic.stocktrader.portfolio.api;

import lombok.Value;

@Value
public class NewPortfolioRequest {
    PortfolioId portfolioId;

    String linkedAccount;
}
