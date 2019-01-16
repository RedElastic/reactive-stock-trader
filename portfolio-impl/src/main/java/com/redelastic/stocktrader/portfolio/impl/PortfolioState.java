package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.math.BigDecimal;


/**
 * We'll encapsulate all the state transition logic here. Each state will provide an overloaded update method which
 * will accept applicable PortfolioEvents.
 */
public interface PortfolioState extends Jsonable {

    @Value
    class Uninitialized implements PortfolioState {
        private Uninitialized() {}
        public static Uninitialized INSTANCE = new Uninitialized();

        Open update(PortfolioEvent.Opened evt) {
            return PortfolioState.Open.builder()
                    .funds(new BigDecimal("0"))
                    .name(evt.getName())
                    .holdings(Holdings.EMPTY)
                    .loyaltyLevel(LoyaltyLevel.BRONZE)
                    .build();
        }
    }

    @Value
    @Builder
    @Wither
    final class Open implements PortfolioState {
        @NonNull BigDecimal funds;
        @NonNull String name;
        @NonNull LoyaltyLevel loyaltyLevel;
        @NonNull Holdings holdings;

        Open update(PortfolioEvent.FundsCredited evt) {
            return this.withFunds(getFunds().add(evt.getAmount()));
        }

        Open update(PortfolioEvent.FundsDebited evt) {
            return this.withFunds(getFunds().subtract(evt.getAmount()));
        }

        Open update(PortfolioEvent.SharesCredited evt) {
            return this.withHoldings(holdings.add(evt.getSymbol(), evt.getShares()));
        }

        Open update(PortfolioEvent.SharesDebited evt) {
            return this.withHoldings(holdings.remove(evt.getSymbol(), evt.getShares()));
        }

        Open update(PortfolioEvent.OrderPlaced evt) {
            return this; // TODO: Track outstanding orders?
        }

    }

    @Value
    @Builder
    final class Liquidating implements PortfolioState {
        @NonNull BigDecimal funds;
        @NonNull String name;
        @NonNull LoyaltyLevel loyaltyLevel;
        @NonNull Holdings holdings;
    }

    enum Closed implements PortfolioState {
        INSTANCE
    }

}