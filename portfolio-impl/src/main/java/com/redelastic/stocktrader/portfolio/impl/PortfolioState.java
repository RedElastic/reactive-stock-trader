package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import play.libs.Json;

import java.math.BigDecimal;

public abstract class PortfolioState implements Jsonable {

    private PortfolioState() {}

    public static final class Uninitialized extends PortfolioState {
        private Uninitialized() {}

        public static final Uninitialized INSTANCE = new Uninitialized();

    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @Builder
    public static final class Open extends PortfolioState {
        BigDecimal funds;

        LoyaltyLevel loyaltyLevel;

        PSequence<Holding> holdings;

        String name;
    }

    public static final class Closed extends PortfolioState {
        private Closed() {}
        public static final Closed INSTANCE = new Closed();
    }

}