package com.redelastic.stocktrader.portfolio.api;

import lombok.Builder;
import lombok.Value;
import org.pcollections.PSequence;

import java.math.BigDecimal;

@Value
@Builder
public class PortfolioView {

    String portfolioId;

    String name;

    BigDecimal funds;

    LoyaltyLevel loyaltyLevel;

    PSequence<ValuedHolding> holdings;
}
