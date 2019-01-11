package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.GetState;
import com.redelastic.stocktrader.portfolio.impl.PortfolioCommand.Open;
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
                .map(state -> {
                    if (state instanceof PortfolioState.Open) {
                        return becomeOpened((PortfolioState.Open)state);
                    } else if (state instanceof PortfolioState.Uninitialized) {
                        return becomeUninitialized();
                    } else {
                        return becomeClosed();
                    }
                })
                .orElse(becomeUninitialized());
    }


    private Behavior becomeUninitialized() {
        BehaviorBuilder builder = newBehaviorBuilder(PortfolioState.Uninitialized.INSTANCE);
        builder.setCommandHandler(Open.class,
                (init, ctx) -> {
                    PortfolioEvent.Opened event = PortfolioEvent.Opened.builder()
                            .name(init.getName())
                            .portfolioId(entityId())
                            .build();
                    log.warn(event.toString());
                    return ctx.thenPersist(
                            PortfolioEvent.Opened.builder()
                                    .name(init.getName())
                                    .portfolioId(entityId())
                                    .build(),
                            (e) -> ctx.reply(Done.getInstance()));
                });
        builder.setEventHandlerChangingBehavior(PortfolioEvent.Opened.class, evt -> {
            log.warn(String.format("Opened %s, named %s", entityId(), evt.getName()));
            PortfolioState.Open state = PortfolioState.Uninitialized.INSTANCE.update(evt);
            return becomeOpened(state);
        });
        return builder.build();
    }
    /**
     * Behaviour for a set up
     * @return
     */
    private Behavior becomeOpened(PortfolioState.Open initialState) {
        return new OpenPortfolioBehavior(initialState).getBehavior();
    }

    private Behavior becomeClosed() {
        return new ClosedPortfolioBehaviourBuilder().getBehavior();
    }


    private void handleLiquidate(BehaviorBuilder builder) {
        builder.setCommandHandler(PortfolioCommand.Liquidate.class, (cmd, ctx) -> {
            // TODO: Sell all stocks, transfer out all funds, then
            return ctx.thenPersist(new PortfolioEvent.LiquidationStarted(entityId()),
                    evt -> ctx.reply(Done.getInstance()));
        });
    }



    abstract class PortfolioBehaviorBuilder<State extends PortfolioState> {
        State state() { return (State)PortfolioEntity.this.state(); }

        abstract Behavior getBehavior();

        void rejectOpen(BehaviorBuilder builder) {
            builder.setCommandHandler(Open.class, (setup, ctx) -> {
                ctx.commandFailed(new PortfolioAlreadyOpened(entityId()));
                return ctx.done();
            });
        }
    }

    private class OpenPortfolioBehavior extends PortfolioBehaviorBuilder<PortfolioState.Open> {
        private final Behavior behavior;

        OpenPortfolioBehavior(PortfolioState.Open state) {
            BehaviorBuilder builder = newBehaviorBuilder(state);
            rejectOpen(builder);
            handlePlaceOrder(builder);
            handleCompletedTrade(builder);
            handleFundEvents(builder);
            handleShareEvents(builder);
            handleGetState(builder);
            handleLiquidate(builder);
            this.behavior = builder.build();
        }

        private void handleCompletedTrade(BehaviorBuilder builder) {
            builder.setCommandHandler(PortfolioCommand.CompleteTrade.class, this::completeTrade);
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

        private void handleFundEvents(BehaviorBuilder builder) {
            builder.setEventHandler(PortfolioEvent.FundsDebited.class, evt -> state().update(evt));
            builder.setEventHandler(PortfolioEvent.FundsCredited.class, evt -> state().update(evt));
        }

        private void handlePlaceOrder(BehaviorBuilder builder) {
            builder.setCommandHandler(PortfolioCommand.PlaceOrder.class, this::placeOrder);

            builder.setEventHandler(PortfolioEvent.OrderPlaced.class, evt -> {
                log.warn("Portfolio entity got OrderPlaced event.");
                return ((PortfolioState.Open)state()).update(evt);

            });
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

        private void handleShareEvents(BehaviorBuilder builder) {
            builder.setEventHandler(PortfolioEvent.SharesCredited.class, evt -> state().update(evt));
            builder.setEventHandler(PortfolioEvent.SharesDebited.class, evt -> state().update(evt));
        }

        private void handleGetState(BehaviorBuilder builder) {
            builder.setReadOnlyCommandHandler(GetState.class, (cmd, ctx) ->
                    ctx.reply(state()));
        }

        Behavior getBehavior() { return this.behavior; }
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
