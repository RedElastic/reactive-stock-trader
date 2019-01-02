package com.redelastic.stocktrader.portfolio.impl.entities;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.Order;
import com.redelastic.stocktrader.broker.api.OrderType;
import com.redelastic.stocktrader.portfolio.api.LoyaltyLevel;
import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;
import com.redelastic.stocktrader.portfolio.impl.entities.PortfolioCommand.*;
import com.redelastic.stocktrader.portfolio.impl.PortfolioState;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

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

    private void rejectOpen(BehaviorBuilder builder) {
        builder.setCommandHandler(Open.class, (setup, ctx) -> {
            ctx.commandFailed(new PortfolioAlreadyInitialized(setup.getPortfolioId()));
            return ctx.done();
        });
    }

    private Behavior becomeUninitialized() {
        BehaviorBuilder builder = newBehaviorBuilder(PortfolioState.Uninitialized.INSTANCE);
        builder.setCommandHandler(Open.class,
                (init, ctx) -> {
                    NewPortfolioRequest req = init.getRequest();
                    return ctx.thenPersist(
                            PortfolioEvent.Opened.builder()
                                    .description(req.getName())
                                    .build(),
                                    (e) -> ctx.reply(Done.getInstance()));
                });
        builder.setEventHandlerChangingBehavior(PortfolioEvent.Opened.class, evt -> {
            log.warn("Opened");
            PortfolioState.Open state = PortfolioState.Open.builder()
                    .funds(new BigDecimal("0"))
                    .name(evt.getDescription())
                    .holdings(TreePVector.empty())
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
    private Behavior becomeOpened(PortfolioState.Open state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);
        rejectOpen(builder);


        builder.setReadOnlyCommandHandler(GetState.class, (cmd, ctx) -> {
            ctx.reply(state);
        });

        return builder.build();
    }

    /**
     * We will confirm that we hold enough shares before initiating the trade. Then remove them from our holdings and
     * transfer them to the broker for sale. This will ensure that we can't sell the same shares twice.
     * @param builder
     */
    private void handleSell(BehaviorBuilder builder) {
        builder.setCommandHandler(SellOrder.class, (sellOrder, ctx) -> {
            Boolean sufficientShares = ((PortfolioState.Open)state()) // FIXME: Is there a type safe way to do this?
                    .getHoldings()
                    .stream()
                    .anyMatch(h -> h.getSymbol() == sellOrder.getSymbol() && h.getShareCount() >= sellOrder.getShares());
            if (sufficientShares) {
                Order order = Order.builder()
                        .symbol(sellOrder.getSymbol())
                        .shares(sellOrder.getShares())
                        .type(OrderType.MarketBuy.INSTANCE)
                        .build();
                // FIXME: LB, is this the right way to handle this?
                return ctx.thenPersist(
                        new PortfolioEvent.SharesTransferToBrokerForSale(sellOrder.getSymbol(), sellOrder.getShares()),
                        evt -> {
                            sellOrder.getBrokerService()
                                    .placeOrder()
                                    .invoke(order)
                                    .thenAccept(d -> ctx.reply(Done.getInstance()));
                        });
            } else {
                ctx.commandFailed(new InsufficientShares(
                        String.format("Unable to place sell order for %s, insufficient shares held.",
                                sellOrder.getSymbol())));
                return ctx.done();
            }
        });
    }

    private Behavior becomeClosed() {
        BehaviorBuilder builder = newBehaviorBuilder(PortfolioState.Closed.INSTANCE);
        rejectOpen(builder);
        return builder.build();
    }



}
