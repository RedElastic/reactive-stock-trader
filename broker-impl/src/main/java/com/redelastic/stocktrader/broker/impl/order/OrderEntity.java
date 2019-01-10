package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public class OrderEntity extends PersistentEntity<OrderCommand, OrderEvent, Optional<OrderState>> {

    private final Logger log = LoggerFactory.getLogger(OrderEntity.class);

    @Override
    public Behavior initialBehavior(Optional<Optional<OrderState>> snapshotState) {
        return snapshotState
                .flatMap(Function.identity())
                .map(orderState -> {
                    if (orderState instanceof OrderState.Pending) {
                        return pendingOrderBehavior(((OrderState.Pending) orderState).getOrderDetails());
                    } else {
                        throw new IllegalStateException();
                    }
                }).orElse(uninitializedBehaviour());
    }

    private Behavior uninitializedBehaviour() {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
        builder.setCommandHandler(OrderCommand.PlaceOrder.class, (cmd,ctx) -> {
            Order order = new Order(entityId(), cmd.getOrderDetails());
            return ctx.thenPersist(
                    new OrderEvent.ProcessingOrder(order),
                    evt -> ctx.reply(order));
        });
        builder.setEventHandlerChangingBehavior(OrderEvent.ProcessingOrder.class,
                evt -> pendingOrderBehavior(evt.getOrder().getDetails()));
        return builder.build();
    }

    private Behavior pendingOrderBehavior(OrderDetails orderDetails) {
        BehaviorBuilder builder = newBehaviorBuilder(
                Optional.of(new OrderState.Pending(orderDetails)));

        builder.setCommandHandler(OrderCommand.Complete.class, (cmd, ctx) -> {

            if (cmd.getOrderResult() instanceof OrderResult.OrderCompleted) {
                OrderResult.OrderCompleted orderCompleted = (OrderResult.OrderCompleted)cmd.getOrderResult();
                return ctx.thenPersist(new OrderEvent.OrderFulfilled(order(), orderCompleted.getTrade()),
                        evt -> ctx.reply(Done.getInstance()));
            } else if (cmd.getOrderResult() instanceof OrderResult.OrderFailed) {
                return ctx.thenPersist(new OrderEvent.OrderFailed(order()),
                        evt -> ctx.reply(Done.getInstance()));
            } else {
                throw new IllegalStateException();
            }
        });
        builder.setEventHandlerChangingBehavior(OrderEvent.OrderFulfilled.class,
                evt -> completedOrderBehavior(
                        new OrderState.Fulfilled(state().get().getOrderDetails(), evt.getTrade().getPrice())));
        handleGetStatus(builder);
        ignoreRepeats(builder);
        return builder.build();
    }

    private Behavior completedOrderBehavior(OrderState orderState) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(orderState));
        handleGetStatus(builder);
        ignoreRepeats(builder);
        return builder.build();
    }

    private void handleGetStatus(BehaviorBuilder builder) {
        builder.setReadOnlyCommandHandler(OrderCommand.GetStatus.class, (cmd,ctx) -> {
            if (!state().isPresent()) {
                ctx.reply(Optional.empty());
            } else {
                ctx.reply(Optional.of(state().get().getStatus()));
            }
        });
    }

    /* Ignore duplicate submissions of an order, provided the details are the same. Duplicates can happen due to
     * at-least-once handling of the order placement process.
     */
    private void ignoreRepeats(BehaviorBuilder builder) {
        builder.setReadOnlyCommandHandler(OrderCommand.PlaceOrder.class, (cmd, ctx) -> {
            OrderDetails orderDetails = cmd.getOrderDetails();
            if (orderDetails.equals(state().get().getOrderDetails())) {
                ctx.reply(order());
            } else {
                log.warn(String.format(
                        "Order %s, existing: %s, received %s",
                        entityId(),
                        state().get().getOrderDetails(),
                        orderDetails.toString()
                ));
                ctx.commandFailed(new InvalidCommandException(
                        String.format("Attempt to place different order with same order ID: %s", entityId())));
            }
        });
    }

    private Order order() {
        return new Order(entityId(), state().get().getOrderDetails());
    }

}
