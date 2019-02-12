/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import java.math.BigDecimal;


/**
 * We'll encapsulate all the state transition logic here. Each state will provide an overloaded update method which
 * will accept applicable PortfolioEvents.
 */
public interface PortfolioState extends Jsonable {

    <T> T visit(Visitor<T> visitor);

    enum Closed implements PortfolioState {
        INSTANCE;

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(INSTANCE);
        }

    }

    interface Visitor<T> {
        T visit(Open open);

        T visit(Liquidating liquidating);

        T visit(Closed closed);
    }

    @Value
    @Builder
    @Wither
    final class Open implements PortfolioState {
        @NonNull BigDecimal funds;
        @NonNull String name;
        @NonNull LoyaltyLevel loyaltyLevel;
        @NonNull Holdings holdings;
        @NonNull PMap<OrderId, PortfolioEvent.OrderPlaced> activeOrders;
        @NonNull PSet<OrderId> completedOrders;

        public static Open initialState(String name) {
            return Open.builder()
                    .name(name)
                    .loyaltyLevel(LoyaltyLevel.BRONZE)
                    .funds(BigDecimal.valueOf(0))
                    .activeOrders(HashTreePMap.empty())
                    .holdings(Holdings.EMPTY)
                    .completedOrders(HashTreePSet.empty())
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

        Open orderCompleted(OrderId orderId) {
            return this
                    .withActiveOrders(activeOrders.minus(orderId))
                    .withCompletedOrders(completedOrders.plus(orderId));
        }


        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Value
    @Builder
    final class Liquidating implements PortfolioState {
        @NonNull BigDecimal funds;
        @NonNull String name;
        @NonNull LoyaltyLevel loyaltyLevel;
        @NonNull Holdings holdings;

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

}