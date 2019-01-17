package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

// TODO: Note overdrawn status on purchase.
class PortfolioEntity extends PersistentEntity<PortfolioCommand, PortfolioEvent, Optional<PortfolioState>> {
    private final Logger log = LoggerFactory.getLogger(PortfolioEntity.class);

    @Override
    public Behavior initialBehavior(Optional<Optional<PortfolioState>> snapshotState) {
        return snapshotState
                .flatMap(Function.identity())
                .map(state -> {
                    if (state instanceof PortfolioState.Open) {
                        return new OpenPortfolioBehavior((PortfolioState.Open) state).getBehavior();
                    } else if (state instanceof PortfolioState.Liquidating) {
                        return new LiquidatingPortfolioBehaviour((PortfolioState.Liquidating) state).getBehavior();
                    } else if (state instanceof PortfolioState.Closed) {
                        return new ClosedPortfolioBehaviourBuilder().getBehavior();
                    } else {
                        throw new IllegalStateException(
                                String.format("Unhandled state %s", state.toString())
                        );
                    }
                })
                .orElse(new UninitializedBehavior().getBehaviour());
    }


    class UninitializedBehavior {

        Behavior getBehaviour() {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());

            builder.setCommandHandler(PortfolioCommand.Open.class, this::open);
            builder.setEventHandlerChangingBehavior(PortfolioEvent.Opened.class, this::opened);

            return builder.build();
        }

        private PersistentEntity.Persist open(PortfolioCommand.Open cmd, CommandContext<Done> ctx) {
            PortfolioEvent.Opened openEvent = PortfolioEvent.Opened.builder()
                    .name(cmd.getName())
                    .portfolioId(entityId())
                    .build();

            log.info(openEvent.toString());
            return ctx.thenPersist(openEvent,
                    (e) -> ctx.reply(Done.getInstance()));
        }

        private Behavior opened(PortfolioEvent.Opened evt) {
            log.info(String.format("Opened %s, named %s", entityId(), evt.getName()));

            return new OpenPortfolioBehavior(evt).getBehavior();
        }
    }


    /**
     * Provides a stronger typed interface for defining state specific behavior when
     * @param <State>
     */
    abstract class PortfolioBehaviorBuilder<State extends PortfolioState> {
        State state() { return (State)PortfolioEntity.this.state().get(); }

        final BehaviorBuilder builder;

        PortfolioBehaviorBuilder(PortfolioState state) {
            builder = newBehaviorBuilder(Optional.of(state));
            builder.setCommandHandler(PortfolioCommand.Open.class, this::rejectOpen);
        }

        Persist rejectOpen(PortfolioCommand.Open cmd, CommandContext<Done> ctx) {
            ctx.commandFailed(new PortfolioAlreadyOpened(entityId()));
            return ctx.done();
        }

        <E extends PortfolioEvent> void setEventHandler(Class<E> event, Function<E, State> handler) {
            builder.setEventHandler(event, handler.andThen(Optional::of));
        }

        <E extends PortfolioEvent> void setEventHandlerChangingState(Class<E> event, Function<E, PortfolioState> handler) {
            Function<E, Behavior> stateHandler = handler.andThen(this::behaviourForState);
            builder.setEventHandlerChangingBehavior(event, stateHandler);
        }

        Behavior getBehavior() {
            return builder.build();
        }

        Behavior behaviourForState(PortfolioState state) {
            if (state instanceof PortfolioState.Open) {
                return new OpenPortfolioBehavior((PortfolioState.Open)state).getBehavior();
            } else if (state instanceof PortfolioState.Liquidating) {
                return new LiquidatingPortfolioBehaviour((PortfolioState.Liquidating)state).getBehavior();
            } else if (state instanceof PortfolioState.Closed) {
                return new ClosedPortfolioBehaviourBuilder().getBehavior();
            } else {
                throw new IllegalStateException();
            }
        }
    }


    private class OpenPortfolioBehavior extends PortfolioBehaviorBuilder<PortfolioState.Open> {

        OpenPortfolioBehavior(PortfolioEvent.Opened evt) {
            this(PortfolioState.Open.builder()
                    .funds(new BigDecimal("0"))
                    .name(evt.getName())
                    .holdings(Holdings.EMPTY)
                    .loyaltyLevel(LoyaltyLevel.BRONZE)
                    .build());
        }

        OpenPortfolioBehavior(PortfolioState.Open initialState) {
            super(initialState);

            builder.setCommandHandler(PortfolioCommand.Open.class, this::rejectOpen);
            builder.setCommandHandler(PortfolioCommand.PlaceOrder.class, this::placeOrder);
            builder.setCommandHandler(PortfolioCommand.CompleteTrade.class, this::completeTrade);
            builder.setCommandHandler(PortfolioCommand.HandleOrderFailure.class, this::handleFailedOrder);
            builder.setCommandHandler(PortfolioCommand.Liquidate.class, this::liquidate);
            builder.setCommandHandler(PortfolioCommand.SendFunds.class, this::sendFunds);
            builder.setCommandHandler(PortfolioCommand.ReceiveFunds.class, this::receiveFunds);

            builder.setReadOnlyCommandHandler(PortfolioCommand.GetState.class, this::getState);

            setEventHandler(PortfolioEvent.OrderPlaced.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.SharesCredited.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.FundsDebited.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.FundsCredited.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.SharesDebited.class, evt -> state().update(evt));

            setEventHandlerChangingState(PortfolioEvent.LiquidationStarted.class, evt ->
                    PortfolioState.Liquidating.builder()
                            .name(state().getName())
                            .funds(state().getFunds())
                            .loyaltyLevel(state().getLoyaltyLevel())
                            .holdings(state().getHoldings())
                            .build()
            );

        }


        private PersistentEntity.Persist completeTrade(PortfolioCommand.CompleteTrade cmd, CommandContext<Done> ctx) {
            Trade trade = cmd.getTrade();
            log.info(String.format(
                    "PortfolioModel %s processing trade %s",
                    entityId(),
                    trade.toString()
            ));
            switch(trade.getOrderType()) {
                case BUY:
                    return ctx.thenPersistAll(Arrays.asList(
                            new PortfolioEvent.FundsDebited(entityId(), trade.getPrice()),
                            new PortfolioEvent.SharesCredited(entityId(), trade.getSymbol(), trade.getShares())),
                            () -> ctx.reply(Done.getInstance()));

                case SELL:
                    // Note: for a sale the shares have already been removed when we initiated the sale
                    return ctx.thenPersist(
                            new PortfolioEvent.FundsCredited(entityId(), trade.getPrice()),
                            evt -> ctx.reply(Done.getInstance()));

                default:
                    throw new IllegalStateException(); // FIXME
            }
        }

        private PersistentEntity.Persist placeOrder(PortfolioCommand.PlaceOrder placeOrder, CommandContext<Done> ctx) {
            log.info(String.format("Placing order %s", placeOrder.toString()));
            OrderDetails orderDetails = placeOrder.getOrderDetails();
            switch(orderDetails.getOrderType()) {
                case SELL:
                    int available = state().getHoldings().getShareCount(orderDetails.getSymbol());
                    if (available >= orderDetails.getShares()) {
                        return ctx.thenPersistAll(Arrays.asList(
                                new PortfolioEvent.OrderPlaced(placeOrder.getOrderId(), entityId(), placeOrder.getOrderDetails()),
                                new PortfolioEvent.SharesDebited(entityId(), orderDetails.getSymbol(), orderDetails.getShares())),
                                () -> ctx.reply(Done.getInstance()));
                    } else {
                        ctx.commandFailed(new InsufficientShares(
                                String.format("Insufficient shares of %s for sell, %d required, %d held.",
                                        orderDetails.getSymbol(),
                                        orderDetails.getShares(),
                                        available)));
                        return ctx.done();
                    }
                case BUY:
                    return ctx.thenPersist(
                            new PortfolioEvent.OrderPlaced(placeOrder.getOrderId(), entityId(), placeOrder.getOrderDetails()),
                            evt -> ctx.reply(Done.getInstance()));
                default:
                    throw new IllegalStateException();
            }
        }

        private void getState(PortfolioCommand.GetState cmd, ReadOnlyCommandContext<PortfolioState.Open> ctx) {
            ctx.reply(state());
        }

        private PersistentEntity.Persist liquidate(PortfolioCommand.Liquidate cmd, CommandContext<Done> ctx) {
            // TODO: Sell all stocks, transfer out all funds, then move to closed.
            // TODO: Handle overdrawn account (negative funds after all equities liquidated
            return ctx.thenPersist(new PortfolioEvent.LiquidationStarted(entityId()),
                    evt -> ctx.reply(Done.getInstance()));
        }

        private PersistentEntity.Persist sendFunds(PortfolioCommand.SendFunds cmd, CommandContext<Done> ctx) {
            if (state().getFunds().compareTo(cmd.getAmount()) >= 0) {
                return ctx.thenPersist(
                        new PortfolioEvent.FundsDebited(entityId(), cmd.getAmount()),
                        evt -> ctx.reply(Done.getInstance())
                );
            } else {
                ctx.commandFailed(new InsufficientFunds(
                        String.format("Attempt to send %.2f, but only %.2f available.", cmd.getAmount(), state().getFunds())));
                return ctx.done();
            }
        }

        private PersistentEntity.Persist receiveFunds(PortfolioCommand.ReceiveFunds cmd, CommandContext<Done> ctx) {
            return ctx.thenPersist(
                    new PortfolioEvent.FundsCredited(entityId(), cmd.getAmount()),
                    evt -> ctx.reply(Done.getInstance())
            );
        }

        private PersistentEntity.Persist handleFailedOrder(PortfolioCommand.HandleOrderFailure cmd, CommandContext<Done> ctx) {
            // TODO: record this
            log.info(String.format("Order %s failed for PortfolioModel %s.", cmd.getOrderFailed().getOrderId(), entityId()));
            ctx.reply(Done.getInstance());
            return ctx.done();
        }

    }

    private class LiquidatingPortfolioBehaviour extends PortfolioBehaviorBuilder<PortfolioState.Liquidating> {

        LiquidatingPortfolioBehaviour(PortfolioState.Liquidating state) {
            super(state);
        }

    }

    private class ClosedPortfolioBehaviourBuilder extends PortfolioBehaviorBuilder<PortfolioState.Closed> {

        ClosedPortfolioBehaviourBuilder() {
            super(PortfolioState.Closed.INSTANCE);
        }

    }

}
