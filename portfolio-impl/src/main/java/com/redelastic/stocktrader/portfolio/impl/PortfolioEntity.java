package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.GetState;
import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.Open;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

// TODO: Note overdrawn status on purchase.

public class PortfolioEntity extends PersistentEntity<PortfolioCommand, PortfolioEvent, PortfolioState> {
    private final Logger log = LoggerFactory.getLogger(PortfolioEntity.class);

    @Override
    public Behavior initialBehavior(Optional<PortfolioState> snapshotState) {
        return snapshotState
                .map(state -> {
                    if (state instanceof PortfolioState.Open) {
                        return becomeOpened(state);
                    } else if (state instanceof PortfolioState.Uninitialized) {
                        return becomeUninitialized();
                    } else {
                        return becomeClosed();
                    }
                })
                .orElse(becomeUninitialized());
    }

    private void rejectOpen(BehaviorBuilder builder) {
        builder.setCommandHandler(Open.class, (setup, ctx) -> {
            ctx.commandFailed(new PortfolioAlreadyInitialized(entityId()));
            return ctx.done();
        });
    }

    private Behavior becomeUninitialized() {
        BehaviorBuilder builder = newBehaviorBuilder(PortfolioState.Uninitialized.INSTANCE);
        builder.setCommandHandler(Open.class,
                (init, ctx) ->
                        ctx.thenPersist(
                            PortfolioEvent.Opened.builder()
                                    .name(init.getName())
                                    .portfolioId(entityId())
                                    .build(),
                                    (e) -> ctx.reply(Done.getInstance()))
                );
        builder.setEventHandlerChangingBehavior(PortfolioEvent.Opened.class, evt -> {
            log.warn("Opened");
            PortfolioState.Open state = PortfolioState.Open.builder()
                    .funds(new BigDecimal("0"))
                    .name(evt.getName())
                    .holdings(Holdings.EMPTY)
                    .loyaltyLevel(LoyaltyLevel.BRONZE)
                    .build();
            return becomeOpened(state);
        });
        return builder.build();
    }
    /**
     * Behaviour for a set up
     * @return
     */
    private Behavior becomeOpened(PortfolioState initialState) {
        BehaviorBuilder builder = newBehaviorBuilder(initialState);
        rejectOpen(builder);
        handlePlaceOrder(builder);
        handleCompletedTrade(builder);
        handleFundEvents(builder);
        handleGetState(builder);
        return builder.build();
    }

    private Behavior becomeClosed() {
        BehaviorBuilder builder = newBehaviorBuilder(PortfolioState.Closed.INSTANCE);
        rejectOpen(builder);
        return builder.build();
    }

    private void handleGetState(BehaviorBuilder builder) {
        builder.setReadOnlyCommandHandler(GetState.class, (cmd, ctx) ->
                ctx.reply(((PortfolioState.Open)state())));
    }

    private void handleCompletedTrade(BehaviorBuilder builder) {
        builder.setCommandHandler(PortfolioCommand.CompleteTrade.class, (cmd, ctx) -> {
            Trade trade = cmd.getTrade();
            switch(trade.getOrderType()) {
                case BUY:
                    return ctx.thenPersistAll(Arrays.asList(
                            new PortfolioEvent.FundsDebited(entityId(), trade.getPrice()),
                            new PortfolioEvent.SharesCredited(entityId(), trade.getSymbol(), trade.getShares())),
                            () -> ctx.reply(Done.getInstance()));

                case SELL:
                    return ctx.thenPersistAll(Arrays.asList(
                            new PortfolioEvent.FundsCredited(entityId(), trade.getPrice()),
                            new PortfolioEvent.SharesDebited(entityId(), trade.getSymbol(), trade.getShares())),
                            () -> ctx.reply(Done.getInstance()));

                default:
                    throw new IllegalStateException(); // FIXME
            }
        });
    }

    private void handleFundEvents(BehaviorBuilder builder) {
        builder.setEventHandler(PortfolioEvent.FundsDebited.class, evt -> {
            PortfolioState.Open currentState = (PortfolioState.Open) state();
            return currentState.withFunds(currentState.getFunds().subtract(evt.getAmount()));
        });
        builder.setEventHandler(PortfolioEvent.FundsCredited.class, evt -> {
            PortfolioState.Open currentState = (PortfolioState.Open) state();
            return currentState.withFunds(currentState.getFunds().add(evt.getAmount()));
        });
    }

    private void handlePlaceOrder(BehaviorBuilder builder) {
        builder.setCommandHandler(PortfolioCommand.PlaceOrder.class, (placeOrder, ctx) -> {
            log.warn("Placing order");
            Order order = placeOrder.getOrder();
            PortfolioState.Open state = (PortfolioState.Open)state();
            switch(order.getOrderType()) {
                case SELL:
                    int available = state.getHoldings().getShareCount(order.getSymbol());
                    if (available >= order.getShares()) {
                        return ctx.thenPersist(new PortfolioEvent.OrderPlaced(entityId(), order),
                                evt -> ctx.reply(Done.getInstance()));
                    } else {
                        ctx.commandFailed(new InsufficientShares(
                                String.format("Insufficient shares of %s for sell, %d required, %d held.",
                                        order.getSymbol(),
                                        order.getShares(),
                                        available)));
                        return ctx.done();
                    }
                case BUY:
                    return ctx.thenPersist(new PortfolioEvent.OrderPlaced(entityId(), order),
                            evt -> ctx.reply(Done.getInstance()));
                default:
                    throw new IllegalStateException();
            }
        });

        builder.setEventHandler(PortfolioEvent.OrderPlaced.class, evt -> {
            log.warn("order event");
            return state(); // TODO: Track outstanding orders
            // TODO: update holdings for a sell
        });
    }

    private void handleLiquidate(BehaviorBuilder builder) {
        builder.setCommandHandler(PortfolioCommand.Liquidate.class, (cmd, ctx) -> {
            // TODO: Sell all stocks, transfer out all funds, then
            return ctx.thenPersist(new PortfolioEvent.LiquidationStarted(entityId()),
                    evt -> ctx.reply(Done.getInstance()));
        });
    }

}
