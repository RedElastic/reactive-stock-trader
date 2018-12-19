package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;

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
     * In this state the portfolio needs to wait for a set up message.
     * @param state
     * @return
     */
    private Behavior uninitialized(PortfolioState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);
        //setupHandler(builder);
        return builder.build();
    }


    private void setupHandler(BehaviorBuilder builder) {
        builder.setCommandHandler(PortfolioCommand.SetupMessage.class,
                (PortfolioCommand.SetupMessage setup, CommandContext ctx) -> {
                    PortfolioId portfolioId = setup.getRequest().getPortfolioId();
                    String linkedAccount = setup.getRequest().getLinkedAccount();

                    return ctx.thenPersist(
                            new PortfolioEvent.Initialized(portfolioId, linkedAccount));
        });
    }

    /**
     * Behaviour for a set up
     * @param state
     * @return
     */
    private Behavior ready(PortfolioState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        return builder.build();
    }
}
