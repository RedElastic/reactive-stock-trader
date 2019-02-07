package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
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
                .map(state ->
                        state.visit(new PortfolioState.Visitor<Behavior>() {
                            @Override
                            public Behavior visit(PortfolioState.Open open) {
                                return new OpenPortfolioBehavior(open).getBehavior();
                            }

                            @Override
                            public Behavior visit(PortfolioState.Liquidating liquidating) {
                                return new LiquidatingPortfolioBehaviour(liquidating).getBehavior();
                            }

                            @Override
                            public Behavior visit(PortfolioState.Closed closed) {
                                return new ClosedPortfolioBehaviourBuilder().getBehavior();
                            }
                        })
                )
                .orElse(new UninitializedBehavior().getBehaviour());
    }

    PortfolioId getPortfolioId() { return new PortfolioId(entityId()); }

    class UninitializedBehavior {

        Behavior getBehaviour() {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());

            builder.setCommandHandler(PortfolioCommand.Open.class, this::open);
            builder.setEventHandlerChangingBehavior(PortfolioEvent.Opened.class, this::opened);
            builder.setReadOnlyCommandHandler(PortfolioCommand.GetState.class, (cmd, ctx) ->
                    ctx.commandFailed(new NotFound(String.format("Portfolio %s not found.", entityId()))));

            return builder.build();
        }

        private PersistentEntity.Persist open(PortfolioCommand.Open cmd, CommandContext<Done> ctx) {
            PortfolioEvent.Opened openEvent = PortfolioEvent.Opened.builder()
                    .name(cmd.getName())
                    .portfolioId(getPortfolioId())
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
     * Provides a stronger typed interface for defining state specific behavior. When we're in a specific state we
     * should have the corresponding behaviour, and vice-versa.
     *
     * @param <State>
     */
    abstract class PortfolioBehaviorBuilder<State extends PortfolioState> {
        final BehaviorBuilder builder;

        PortfolioBehaviorBuilder(PortfolioState state) {
            builder = newBehaviorBuilder(Optional.of(state));
            builder.setCommandHandler(PortfolioCommand.Open.class, this::rejectOpen);
        }

        // Shadow parent state() method with behaviour specific state so we don't need downcasts throughout.
        @SuppressWarnings("unchecked")
        State state() { return (State) PortfolioEntity.this.state().get(); }

        Persist rejectOpen(PortfolioCommand.Open cmd, CommandContext<Done> ctx) {
            ctx.commandFailed(new PortfolioAlreadyOpened(getPortfolioId()));
            return ctx.done();
        }

        //
        <E extends PortfolioEvent> void setEventHandler(Class<E> event, Function<E, State> handler) {
            builder.setEventHandler(event, handler.andThen(Optional::of));
        }

        // Let us change behaviours by switching to the corresponding state
        <E extends PortfolioEvent> void setEventHandlerChangingState(Class<E> event, Function<E, PortfolioState> handler) {
            Function<E, Behavior> stateHandler = handler.andThen(this::behaviourForState);
            builder.setEventHandlerChangingBehavior(event, stateHandler);
        }

        Behavior getBehavior() {
            return builder.build();
        }

        Behavior behaviourForState(PortfolioState state) {
            return state.visit(new PortfolioState.Visitor<Behavior>() {
                @Override
                public Behavior visit(PortfolioState.Open open) {
                    return new OpenPortfolioBehavior(open).getBehavior();
                }

                @Override
                public Behavior visit(PortfolioState.Liquidating liquidating) {
                    return new LiquidatingPortfolioBehaviour(liquidating).getBehavior();
                }

                @Override
                public Behavior visit(PortfolioState.Closed closed) {
                    return new ClosedPortfolioBehaviourBuilder().getBehavior();
                }
            });
        }
    }

    private class OpenPortfolioBehavior extends PortfolioBehaviorBuilder<PortfolioState.Open> {

        OpenPortfolioBehavior(PortfolioEvent.Opened evt) {
            this(PortfolioState.Open.initialState(evt.getName()));
        }

        OpenPortfolioBehavior(PortfolioState.Open initialState) {
            super(initialState);

            builder.setCommandHandler(PortfolioCommand.Open.class, this::rejectOpen);
            builder.setCommandHandler(PortfolioCommand.PlaceOrder.class, this::placeOrder);
            builder.setCommandHandler(PortfolioCommand.CompleteTrade.class, this::completeTrade);
            builder.setCommandHandler(PortfolioCommand.AcknowledgeOrderFailure.class, this::handleFailedOrder);
            builder.setCommandHandler(PortfolioCommand.Liquidate.class, this::liquidate);
            builder.setCommandHandler(PortfolioCommand.SendFunds.class, this::sendFunds);
            builder.setCommandHandler(PortfolioCommand.ReceiveFunds.class, this::receiveFunds);
            builder.setCommandHandler(PortfolioCommand.AcceptRefund.class, this::acceptRefund);
            builder.setCommandHandler(PortfolioCommand.ClosePortfolio.class, this::closePortfolio);

            builder.setReadOnlyCommandHandler(PortfolioCommand.GetState.class, this::getState);

            setEventHandler(PortfolioEvent.OrderPlaced.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.SharesCredited.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.FundsDebited.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.FundsCredited.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.SharesDebited.class, evt -> state().update(evt));
            setEventHandler(PortfolioEvent.OrderFulfilled.class, evt ->
                    state().orderCompleted(evt.getOrderId()));
            setEventHandler(PortfolioEvent.OrderFailed.class, evt ->
                    state().orderCompleted(evt.getOrderId()));

            setEventHandlerChangingState(PortfolioEvent.LiquidationStarted.class, evt ->
                    PortfolioState.Liquidating.builder()
                            .name(state().getName())
                            .funds(state().getFunds())
                            .loyaltyLevel(state().getLoyaltyLevel())
                            .holdings(state().getHoldings())
                            .build()
            );

        }

        private Persist acceptRefund(PortfolioCommand.AcceptRefund cmd, CommandContext<Done> ctx) {
            return ctx.thenPersist(
                    new PortfolioEvent.RefundAccepted(getPortfolioId(), cmd.getTransferId(), cmd.getAmount()),
                    evt -> ctx.reply(Done.getInstance())
            );
        }

        private Persist closePortfolio(PortfolioCommand.ClosePortfolio cmd, CommandContext<Done> ctx) {
            if (isEmpty()) {
                return ctx.thenPersist(
                        new PortfolioEvent.Closed(getPortfolioId()),
                        evt -> ctx.reply(Done.getInstance())
                );
            } else {
                ctx.commandFailed(new IllegalStateException("Portfolio is not empty"));
                return ctx.done();
            }
        }

        private boolean isEmpty() {
            return state().getFunds().compareTo(BigDecimal.ZERO) == 0
                    && state().getHoldings().asSequence().isEmpty()
                    && state().getActiveOrders().isEmpty();
        }


        private PersistentEntity.Persist completeTrade(PortfolioCommand.CompleteTrade cmd, CommandContext<Done> ctx) {
            Trade trade = cmd.getTrade();
            OrderId orderId = cmd.getOrderId();
            log.info(String.format(
                    "PortfolioModel %s processing trade %s",
                    entityId(),
                    trade.toString()
            ));
            if (state().getActiveOrders().get(orderId) == null) {
                // This is a trade for an order that we don't believe to be active, presumably we've already processed
                // the result of this order and this is a duplicate result.
                // TODO: More complete tracking so we know that we're not dropping trades
                ctx.reply(Done.getInstance());
                return ctx.done();
            } else {
                switch (trade.getTradeType()) {
                    case BUY:
                        return ctx.thenPersistAll(Arrays.asList(
                                new PortfolioEvent.FundsDebited(getPortfolioId(), trade.getSharePrice()),
                                new PortfolioEvent.SharesCredited(getPortfolioId(), trade.getSymbol(), trade.getShares()),
                                new PortfolioEvent.OrderFulfilled(getPortfolioId(), orderId)),
                                () -> ctx.reply(Done.getInstance()));

                    case SELL:
                        return ctx.thenPersistAll(Arrays.asList(
                                // Note: for a sale the shares have already been removed when we initiated the sale
                                new PortfolioEvent.FundsCredited(getPortfolioId(), trade.getSharePrice()),
                                new PortfolioEvent.OrderFulfilled(getPortfolioId(), orderId)),
                                () -> ctx.reply(Done.getInstance()));

                    default:
                        throw new IllegalStateException();
                }
            }
        }

        private PersistentEntity.Persist placeOrder(PortfolioCommand.PlaceOrder placeOrder, CommandContext<Done> ctx) {
            log.info(String.format("Placing order %s", placeOrder.toString()));
            OrderDetails orderDetails = placeOrder.getOrderDetails();
            switch (orderDetails.getTradeType()) {
                case SELL:
                    int available = state().getHoldings().getShareCount(orderDetails.getSymbol());
                    if (available >= orderDetails.getShares()) {
                        return ctx.thenPersistAll(Arrays.asList(
                                new PortfolioEvent.OrderPlaced(placeOrder.getOrderId(), getPortfolioId(), placeOrder.getOrderDetails()),
                                new PortfolioEvent.SharesDebited(getPortfolioId(), orderDetails.getSymbol(), orderDetails.getShares())),
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
                            new PortfolioEvent.OrderPlaced(placeOrder.getOrderId(), getPortfolioId(), placeOrder.getOrderDetails()),
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
            return ctx.thenPersist(new PortfolioEvent.LiquidationStarted(getPortfolioId()),
                    evt -> ctx.reply(Done.getInstance()));
        }

        private PersistentEntity.Persist sendFunds(PortfolioCommand.SendFunds cmd, CommandContext<Done> ctx) {
            if (state().getFunds().compareTo(cmd.getAmount()) >= 0) {
                return ctx.thenPersist(
                        new PortfolioEvent.FundsDebited(getPortfolioId(), cmd.getAmount()),
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
                    new PortfolioEvent.FundsCredited(getPortfolioId(), cmd.getAmount()),
                    evt -> ctx.reply(Done.getInstance())
            );
        }

        /**
         * If a sell order failed then we can reclaim the shares.
         *
         * @param cmd
         * @param ctx
         * @return
         */
        private PersistentEntity.Persist handleFailedOrder(PortfolioCommand.AcknowledgeOrderFailure cmd, CommandContext<Done> ctx) {
            log.info(String.format("Order %s failed for PortfolioModel %s.", cmd.getOrderFailed().getOrderId(), entityId()));
            PortfolioEvent.OrderPlaced orderPlaced = state().getActiveOrders().get(cmd.getOrderFailed().getOrderId());
            if (orderPlaced == null) {
                // Not currently an active order, this may be a duplicate message.
                log.info(String.format("Order failure for order %s, which is not currently active.", cmd.getOrderFailed().getOrderId()));
                ctx.reply(Done.getInstance());
                return ctx.done();
            } else {
                log.info(String.format("Order failure for order %s.", cmd.getOrderFailed().getOrderId()));
                switch (orderPlaced.getOrderDetails().getTradeType()) {
                    case SELL:
                        return ctx.thenPersistAll(Arrays.asList(
                                new PortfolioEvent.SharesCredited(getPortfolioId(), orderPlaced.getOrderDetails().getSymbol(), orderPlaced.getOrderDetails().getShares()),
                                new PortfolioEvent.OrderFailed(getPortfolioId(), cmd.getOrderFailed().getOrderId())
                        ), () -> ctx.reply(Done.getInstance()));
                    case BUY:
                        return ctx.thenPersist(
                                new PortfolioEvent.OrderFailed(getPortfolioId(), cmd.getOrderFailed().getOrderId()),
                                evt -> ctx.reply(Done.getInstance()));
                    default:
                        throw new IllegalStateException();
                }

            }

        }

    }

    /**
     * Once we've entered the liquidating state we're waiting for the shares of our stocks to be sold. Once this
     * happens we can transfer the funds out of the portfolio and close it.
     */
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
