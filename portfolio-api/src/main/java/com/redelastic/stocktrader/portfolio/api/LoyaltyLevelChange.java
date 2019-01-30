package com.redelastic.stocktrader.portfolio.api;

import com.redelastic.stocktrader.PortfolioId;
import lombok.NonNull;
import lombok.Value;

@Value
class LoyaltyLevelChange {
    @NonNull PortfolioId portfolioId;

    @NonNull LoyaltyLevel oldLoyaltyLevel;

    @NonNull LoyaltyLevel newLoyaltyLevel;
}
