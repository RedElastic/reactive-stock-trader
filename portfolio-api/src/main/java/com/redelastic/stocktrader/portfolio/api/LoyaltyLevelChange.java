package com.redelastic.stocktrader.portfolio.api;

import lombok.Value;

@Value
public class LoyaltyLevelChange {
    String portfolioId;

    LoyaltyLevel oldLoyaltyLevel;

    LoyaltyLevel newLoyaltyLevel;
}
