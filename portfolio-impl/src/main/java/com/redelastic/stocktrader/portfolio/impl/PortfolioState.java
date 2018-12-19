package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.Holding;
import lombok.Value;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

@Value
public final class PortfolioState implements Jsonable {

    int funds;

    PSequence<Holding> holdings;

    public static PortfolioState uninitialized() {
        return new PortfolioState(0, TreePVector.empty());
    }

}
