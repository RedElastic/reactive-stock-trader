package com.redelastic.stocktrader.portfolio.api;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
public class PortfolioView {

    PortfolioId portfolioId;

    BigDecimal funds;

    LoyaltyLevel loyaltyLevel;

    List<Holding> holdings;
}
