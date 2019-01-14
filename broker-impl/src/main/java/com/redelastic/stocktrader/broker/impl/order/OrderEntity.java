package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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
                        return new PendingBehaviorBuilder((OrderState.Pending) orderState).getBehavior();
                    } else if (orderState instanceof OrderState.Fulfilled) {
                        return new FulfilledOrderBehaviorBuilder((OrderState.Fulfilled) orderState).getBehavior();
                    } else if (orderState instanceof OrderState.Failed) {
                        throw new IllegalStateException(); // FIXME: implement this behavior
                    } else {
                        throw new IllegalStateException();
                    }
                }).orElse(new UninitializedBehaviorBuilder().getBehavior());
    }


    private interface OrderBehaviorBuilder {
        Behavior getBehavior();
    }

    /**
     * Base class for OrderBehavior covering pending and completed orders. Not this is not completely type safe, it is
     * possible to set an event handler that produces a state that doesn't correspond to the current behaviour.
     * @param <State> The type of state associated to this behavior.
     */
    private abstract class OrderBehaviourBuilder<State extends OrderState> implements OrderBehaviorBuilder {

        String entityId() { return OrderEntity.this.entityId(); }
        Logger getLogger() { return OrderEntity.this.log; }

        OrderDetails getOrderDetails() { return state().getOrderDetails(); }

        Order getOrder() { return new Order(entityId(), getOrderDetails()); }

        State state() { return (State)OrderEntity.this.state().get(); }

        void getStatus(OrderCommand.GetStatus cmd, ReadOnlyCommandContext ctx) {
            ctx.reply(Optional.of(state().getStatus()));
        }

        void ignoreDuplicatePlacements(OrderCommand.PlaceOrder cmd, ReadOnlyCommandContext<Order> ctx) {
            OrderDetails orderDetails = cmd.getOrderDetails();
            if (orderDetails.equals(getOrderDetails())) {
                ctx.reply(getOrder());
            } else {
                getLogger().warn(String.format(
                        "Order %s, existing: %s, received %s",
                        entityId(),
                        getOrderDetails(),
                        orderDetails.toString()
                ));
                ctx.commandFailed(new InvalidCommandException(
                        String.format("Attempt to place different order with same order ID: %s", entityId())));
            }
        }

        /**
         * Concrete behavior builders should invoke this to add the common behavior implemented here.
         * @param builder
         */
        void setCommonBehavior(BehaviorBuilder builder) {
            builder.setReadOnlyCommandHandler(OrderCommand.GetStatus.class, this::getStatus);
            builder.setReadOnlyCommandHandler(OrderCommand.PlaceOrder.class, this::ignoreDuplicatePlacements);
        }
    }


    private class PendingBehaviorBuilder extends OrderBehaviourBuilder<OrderState.Pending> {

        private final Behavior behavior;

        PendingBehaviorBuilder(OrderState.Pending state) {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state));
            setCommonBehavior(builder);

            builder.setCommandHandler(OrderCommand.Complete.class, this::complete);
            builder.setEventHandlerChangingBehavior(OrderEvent.OrderFulfilled.class, this::fulfilled);
            this.behavior = builder.build();
        }

        PendingBehaviorBuilder(OrderDetails orderDetails) {
            this(new OrderState.Pending(orderDetails));
        }

        private Persist complete(OrderCommand.Complete cmd, CommandContext<Done> ctx) {
            if (cmd.getOrderResult() instanceof OrderResult.OrderFulfilled) {
                OrderResult.OrderFulfilled orderFulfilled = (OrderResult.OrderFulfilled)cmd.getOrderResult();
                return ctx.thenPersist(new OrderEvent.OrderFulfilled(getOrder(), orderFulfilled.getTrade()),
                        evt -> ctx.reply(Done.getInstance()));
            } else if (cmd.getOrderResult() instanceof OrderResult.OrderFailed) {
                return ctx.thenPersist(new OrderEvent.OrderFailed(getOrder()),
                        evt -> ctx.reply(Done.getInstance()));
            } else {
                throw new IllegalStateException();
            }
        }

        private Behavior fulfilled(OrderEvent.OrderFulfilled evt) {
            return new FulfilledOrderBehaviorBuilder(state().getOrderDetails(), evt.getTrade().getPrice()).getBehavior();
        }

        public Behavior getBehavior() {
            return behavior;
        }

    }

    private class FulfilledOrderBehaviorBuilder extends OrderBehaviourBuilder<OrderState.Fulfilled> {

        private final Behavior behavior;

        FulfilledOrderBehaviorBuilder(OrderState.Fulfilled state) {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state));
            setCommonBehavior(builder);

            this.behavior = builder.build();
        }

        FulfilledOrderBehaviorBuilder(OrderDetails orderDetails, BigDecimal price) {
            this(new OrderState.Fulfilled(orderDetails, price));
        }

        public Behavior getBehavior() {

            return this.behavior;
        }
    }

    private class UninitializedBehaviorBuilder implements OrderBehaviorBuilder {
        private final Behavior behavior;

        UninitializedBehaviorBuilder() {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
            builder.setCommandHandler(OrderCommand.PlaceOrder.class, this::placeOrder);
            builder.setEventHandlerChangingBehavior(OrderEvent.ProcessingOrder.class, this::processing);
            this.behavior = builder.build();
        }

        public Behavior getBehavior() {
            return behavior;
        }

        private Persist placeOrder(OrderCommand.PlaceOrder cmd, CommandContext<Order> ctx) {
            Order order = new Order(entityId(), cmd.getOrderDetails());
            return ctx.thenPersist(
                    new OrderEvent.ProcessingOrder(order),
                    evt -> ctx.reply(order));
        }

        private Behavior processing(OrderEvent.ProcessingOrder evt) {
            return new PendingBehaviorBuilder(evt.getOrder().getDetails()).getBehavior();
        }
    }


}
