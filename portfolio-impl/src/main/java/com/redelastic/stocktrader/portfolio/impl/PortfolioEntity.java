package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

// TODO: Note overdrawn status on purchase.
public class PortfolioEntity extends PersistentEntity<PortfolioCommand, PortfolioEvent, PortfolioState> {
    private final Logger log = LoggerFactory.getLogger(PortfolioEntity.class);

    @Override
    public Behavior initialBehavior(Optional<PortfolioState> snapshotState) {
        return snapshotState
                .filter(state -> !(state instanceof PortfolioState.Uninitialized))
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
            BehaviorBuilder builder = newBehaviorBuilder(PortfolioState.Uninitialized.INSTANCE);

            builder.setCommandHandler(PortfolioCommand.Open.class, this::open);
            builder.setEventHandlerChangingBehavior(PortfolioEvent.Opened.class, this::opened);

            return builder.build();
        }

        private Persist open(PortfolioCommand.Open cmd, CommandContext<Done> ctx) {
            PortfolioEvent.Opened event = PortfolioEvent.Opened.builder()
                    .name(cmd.getName())
                    .portfolioId(entityId())
                    .build();
            log.warn(event.toString());
            return ctx.thenPersist(
                    PortfolioEvent.Opened.builder()
                            .name(cmd.getName())
                            .portfolioId(entityId())
                            .build(),
                    (e) -> ctx.reply(Done.getInstance()));
        }

        private Behavior opened(PortfolioEvent.Opened evt) {
            log.warn(String.format("Opened %s, named %s", entityId(), evt.getName()));
            PortfolioState.Open state = PortfolioState.Uninitialized.INSTANCE.update(evt);
            return new OpenPortfolioBehavior(state).getBehavior();
        }
    }



    abstract class PortfolioBehaviorBuilder<State extends PortfolioState> {
        State state() { return (State)PortfolioEntity.this.state(); }

        abstract Behavior getBehavior();

        void rejectOpen(BehaviorBuilder builder) {
            builder.setCommandHandler(PortfolioCommand.Open.class, this::rejectOpen);
        }

        Persist rejectOpen(PortfolioCommand.Open cmd, CommandContext<Done> ctx) {
            ctx.commandFailed(new PortfolioAlreadyOpened(entityId()));
            return ctx.done();
        }
    }


    private class OpenPortfolioBehavior extends PortfolioBehaviorBuilder<PortfolioState.Open> {
        private final Behavior behavior;

        OpenPortfolioBehavior(PortfolioState.Open state) {
            BehaviorBuilder builder = newBehaviorBuilder(state);

            builder.setCommandHandler(PortfolioCommand.Open.class, this::rejectOpen);
            builder.setCommandHandler(PortfolioCommand.PlaceOrder.class, this::placeOrder);
            builder.setCommandHandler(PortfolioCommand.CompleteTrade.class, this::completeTrade);
            builder.setCommandHandler(PortfolioCommand.HandleOrderFailure.class, this::handleFailedOrder);
            builder.setCommandHandler(PortfolioCommand.Liquidate.class, this::liquidate);
            builder.setCommandHandler(PortfolioCommand.SendFunds.class, this::sendFunds);
            builder.setCommandHandler(PortfolioCommand.ReceiveFunds.class, this::receiveFunds);

            builder.setReadOnlyCommandHandler(PortfolioCommand.GetState.class, this::getState);

            builder.setEventHandler(PortfolioEvent.OrderPlaced.class, evt -> state().update(evt));
            builder.setEventHandler(PortfolioEvent.SharesCredited.class, evt -> state().update(evt));
            builder.setEventHandler(PortfolioEvent.FundsDebited.class, evt -> state().update(evt));
            builder.setEventHandler(PortfolioEvent.FundsCredited.class, evt -> state().update(evt));
            builder.setEventHandler(PortfolioEvent.SharesDebited.class, evt -> state().update(evt));

            // TODO: builder.setEventHandlerChangingBehavior(PortfolioEvent.LiquidationStarted.class, evt -> );

            this.behavior = builder.build();
        }


        private Persist completeTrade(PortfolioCommand.CompleteTrade cmd, CommandContext<Done> ctx) {
            Trade trade = cmd.getTrade();
            log.warn(String.format(
                    "Portfolio %s processing trade %s",
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
                    return ctx.thenPersistAll(Arrays.asList(
                            new PortfolioEvent.FundsCredited(entityId(), trade.getPrice())),
                            () -> ctx.reply(Done.getInstance()));

                default:
                    throw new IllegalStateException(); // FIXME
            }
        }

        private Persist placeOrder(PortfolioCommand.PlaceOrder placeOrder, CommandContext<Done> ctx) {
            log.warn(String.format("Placing order %s", placeOrder.getOrder().toString()));
            Order order = placeOrder.getOrder();
            OrderDetails orderDetails = placeOrder.getOrder().getDetails();
            PortfolioState.Open state = (PortfolioState.Open)state();
            switch(orderDetails.getOrderType()) {
                case SELL:
                    int available = state.getHoldings().getShareCount(orderDetails.getSymbol());
                    if (available >= orderDetails.getShares()) {
                        return ctx.thenPersistAll(Arrays.asList(
                                new PortfolioEvent.OrderPlaced(entityId(), order),
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
                    return ctx.thenPersist(new PortfolioEvent.OrderPlaced(entityId(), order),
                            evt -> ctx.reply(Done.getInstance()));
                default:
                    throw new IllegalStateException();
            }
        }

        private void getState(PortfolioCommand.GetState cmd, ReadOnlyCommandContext<PortfolioState.Open> ctx) {
            ctx.reply(state());
        }

        private Persist liquidate(PortfolioCommand.Liquidate cmd, CommandContext<Done> ctx) {
            // TODO: Sell all stocks, transfer out all funds, then move to closed.
            // TODO: Handle overdrawn account (negative funds after all equities liquidated
            return ctx.thenPersist(new PortfolioEvent.LiquidationStarted(entityId()),
                    evt -> ctx.reply(Done.getInstance()));
        }

        private Persist sendFunds(PortfolioCommand.SendFunds cmd, CommandContext<Done> ctx) {
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

        private Persist receiveFunds(PortfolioCommand.ReceiveFunds cmd, CommandContext<Done> ctx) {
            return ctx.thenPersist(
                    new PortfolioEvent.FundsCredited(entityId(), cmd.getAmount()),
                    evt -> ctx.reply(Done.getInstance())
            );
        }

        private Persist handleFailedOrder(PortfolioCommand.HandleOrderFailure cmd, CommandContext<Done> ctx) {
            // TODO: record this
            log.info(String.format("Order %s failed for Portfolio %s.", cmd.getOrderFailed().getOrderId(), entityId()));
            ctx.reply(Done.getInstance());
            return ctx.done();
        }

        Behavior getBehavior() { return this.behavior; }
    }

    private class LiquidatingPortfolioBehaviour extends PortfolioBehaviorBuilder<PortfolioState.Liquidating> {

        private final Behavior behavior;

        LiquidatingPortfolioBehaviour(PortfolioState.Liquidating state) {
            BehaviorBuilder builder = newBehaviorBuilder(state);
            this.behavior = builder.build();
        }

        LiquidatingPortfolioBehaviour(PortfolioState.Open openState) {
            this(PortfolioState.Liquidating.builder()
                    .name(openState.getName())
                    .funds(openState.getFunds())
                    .holdings(openState.getHoldings())
                    .loyaltyLevel(openState.getLoyaltyLevel())
                    .build());
        }

        @Override
        Behavior getBehavior() {
            return behavior;
        }
    }

    private class ClosedPortfolioBehaviourBuilder extends PortfolioBehaviorBuilder<PortfolioState.Closed> {
        private final Behavior behavior;

        ClosedPortfolioBehaviourBuilder() {
            BehaviorBuilder builder = newBehaviorBuilder(PortfolioState.Closed.INSTANCE);
            rejectOpen(builder);

            this.behavior = builder.build();
        }

        @Override
        Behavior getBehavior() {
            return behavior;
        }
    }

}
