package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import lombok.Value;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.math.BigDecimal;

@Value
public class PortfolioState implements Jsonable {

    BigDecimal funds;

    LoyaltyLevel loyaltyLevel;

    PSequence<Holding> holdings;

    public static PortfolioState uninitialized() {
        return new PortfolioState(new BigDecimal("0"), LoyaltyLevel.BRONZE, TreePVector.empty());
    }

}
