package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;
import org.pcollections.PSequence;

import java.math.BigDecimal;

public interface PortfolioState extends Jsonable {

    enum Uninitialized implements PortfolioState {
        INSTANCE
    }

    @Value
    @Builder
    @Wither
    final class Open implements PortfolioState {
        BigDecimal funds;
        String name;
        LoyaltyLevel loyaltyLevel;
        Holdings holdings;

        public Open addShares(String symbol, int shares) {
            return this.withHoldings(holdings.add(symbol, shares));
        }
    }

    @Value
    @Builder
    final class Liquidating implements PortfolioState {
        BigDecimal funds;
        String name;
        LoyaltyLevel loyaltyLevel;
        PSequence<Holding> holdings;
    }

    enum Closed implements PortfolioState {
        INSTANCE
    }

}