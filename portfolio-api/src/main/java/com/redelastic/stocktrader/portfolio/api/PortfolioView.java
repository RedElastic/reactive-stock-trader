package com.redelastic.stocktrader.portfolio.api;

import lombok.Value;
import org.pcollections.PSequence;

import java.math.BigDecimal;

@Value
public class PortfolioView {

    PortfolioId portfolioId;

    BigDecimal funds;

    LoyaltyLevel loyaltyLevel;

    PSequence<ValuedHolding> holdings;
}
