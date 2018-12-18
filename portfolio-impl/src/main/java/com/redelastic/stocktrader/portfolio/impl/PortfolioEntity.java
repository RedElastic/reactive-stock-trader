package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

public class PortfolioEntity extends PersistentEntity<PortfolioCommand, PortfolioEvent, PortfolioState> {
    @Override
    public Behavior initialBehavior(Optional<PortfolioState> snapshotState) {
        if (!snapshotState.isPresent()) {
            return uninitialized(PortfolioState.uninitialized());
        } else {
            PortfolioState state = snapshotState.get();
            return ready(state);
        }
    }

    /**
     * Behaviour for freshly created portfolio which hasn't yet been set up.
     * @param state
     * @return
     */
    private Behavior uninitialized(PortfolioState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        return builder.build();
    }

    private Behavior ready(PortfolioState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        return builder.build();
    }
}
