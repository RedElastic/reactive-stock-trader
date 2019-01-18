package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.math.BigDecimal;


/**
 * We'll encapsulate all the state transition logic here. Each state will provide an overloaded update method which
 * will accept applicable PortfolioEvents.
 */
public interface PortfolioState extends Jsonable {

    @Value
    @Builder
    @Wither
    final class Open implements PortfolioState {
        @NonNull BigDecimal funds;
        @NonNull String name;
        @NonNull LoyaltyLevel loyaltyLevel;
        @NonNull Holdings holdings;
        @NonNull PMap<String, PortfolioEvent.OrderPlaced> activeOrders;

        public static Open initialState(String name) {
            return Open.builder()
                    .name(name)
                    .loyaltyLevel(LoyaltyLevel.BRONZE)
                    .funds(BigDecimal.valueOf(0))
                    .activeOrders(HashTreePMap.empty())
                    .holdings(Holdings.EMPTY)
                    .build();
        }

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
            return this.withActiveOrders(activeOrders.plus(evt.getOrderId(), evt));
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