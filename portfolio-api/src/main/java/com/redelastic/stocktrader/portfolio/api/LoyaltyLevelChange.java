package com.redelastic.stocktrader.portfolio.api;

import lombok.NonNull;
import lombok.Value;

@Value
public class LoyaltyLevelChange {
    @NonNull String portfolioId;

    @NonNull LoyaltyLevel oldLoyaltyLevel;

    @NonNull LoyaltyLevel newLoyaltyLevel;
}
