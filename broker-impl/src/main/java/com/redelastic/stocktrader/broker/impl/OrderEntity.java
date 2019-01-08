package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.order.Order;

import java.util.Optional;
import java.util.function.Function;

public class OrderEntity extends PersistentEntity<OrderCommand, OrderEvent, Optional<OrderState>> {

    @Override
    public Behavior initialBehavior(Optional<Optional<OrderState>> snapshotState) {
        return snapshotState
                .flatMap(Function.identity())
                .map(orderState -> {
                    if (orderState instanceof OrderState.Pending) {
                        return pendingOrderBehavior(((OrderState.Pending) orderState).getOrder());
                    } else {
                        throw new IllegalStateException();
                    }
                }).orElse(uninitializedBehaviour());
    }

    private Behavior uninitializedBehaviour() {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
        builder.setCommandHandler(OrderCommand.PlaceOrder.class, (cmd,ctx) ->
            ctx.thenPersist(new OrderEvent.ProcessingOrder(cmd.getOrder()),
                    evt -> ctx.reply(Done.getInstance())));
        builder.setEventHandlerChangingBehavior(OrderEvent.ProcessingOrder.class,
                evt -> pendingOrderBehavior(evt.getOrder()));
        return builder.build();
    }

    private Behavior pendingOrderBehavior(Order order) {
        BehaviorBuilder builder = newBehaviorBuilder(
                Optional.of(new OrderState.Pending(order)));
        builder.setCommandHandler(OrderCommand.PlaceOrder.class, (cmd, ctx) -> {
            if (cmd.getOrder() == state().get().getOrder()) {
                ctx.reply(Done.getInstance());
                return ctx.done();
            } else {
                ctx.commandFailed(new InvalidCommandException(
                        String.format("Attempt to place different order with same order ID: %s", order.getOrderId())));
                return ctx.done();
            }
        });
        builder.setCommandHandler(OrderCommand.Complete.class, (cmd, ctx) -> {
            if (cmd.getOrderResult() instanceof OrderResult.OrderCompleted) {
                OrderResult.OrderCompleted orderCompleted = (OrderResult.OrderCompleted)cmd.getOrderResult();

                return ctx.thenPersist(new OrderEvent.OrderFulfilled(order, orderCompleted.getTrade()),
                        evt -> ctx.reply(Done.getInstance()));
            } else if (cmd.getOrderResult() instanceof OrderResult.OrderFailed) {
                return ctx.thenPersist(new OrderEvent.OrderFailed(order),
                        evt -> ctx.reply(Done.getInstance()));
            } else {
                throw new IllegalStateException();
            }
        });
        builder.setEventHandlerChangingBehavior(OrderEvent.OrderFulfilled.class,
                evt -> completedOrderBehavior(
                        new OrderState.Fulfilled(state().get().getOrder(), evt.getTrade().getPrice())));
        return builder.build();
    }

    private Behavior completedOrderBehavior(OrderState orderState) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(orderState));
        return builder.build();
    }


}
