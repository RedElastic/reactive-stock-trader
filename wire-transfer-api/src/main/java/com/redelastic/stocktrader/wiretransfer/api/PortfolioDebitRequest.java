package com.redelastic.stocktrader.wiretransfer.api;

import com.redelastic.stocktrader.portfolio.api.PortfolioId;
import lombok.Value;

@Value
public class PortfolioDebitRequest {
    Account source;
    PortfolioId portfolioId;
}
