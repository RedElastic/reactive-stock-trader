package com.redelastic.stocktrader.portfolio.api;

import lombok.Builder;
import lombok.Value;
import org.pcollections.PSequence;

import java.math.BigDecimal;

@Value
@Builder
public class PortfolioView {

    PortfolioId portfolioId;

    BigDecimal funds;

    LoyaltyLevel loyaltyLevel;

    PSequence<ValuedHolding> holdings;
}
