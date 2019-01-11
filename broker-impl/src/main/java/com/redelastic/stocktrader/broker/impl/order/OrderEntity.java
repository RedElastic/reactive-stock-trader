package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.OrderStatus;
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
                        return new PendingBehaviorBuilder((OrderState.Pending)orderState).getBehavior();
                    } else if (orderState instanceof OrderState.Fulfilled) {
                        return new FulfilledOrderBehaviorBuilder((OrderState.Fulfilled) orderState).getBehavior();
                    } else if (orderState instanceof OrderState.Failed) {
                        throw new IllegalStateException(); // FIXME: implement this behavior
                    } else {
                        throw new IllegalStateException();
                    }
                }).orElse(new UninitializedBehaviorBuilder().getBehavior());
    }
/*
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
        return new PendingBehaviorBuilder(orderDetails).getBehavior();

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


    /*private void handleGetStatus(BehaviorBuilder builder) {
        builder.setReadOnlyCommandHandler(OrderCommand.GetStatus.class, (cmd,ctx) -> {
            if (!state().isPresent()) {
                ctx.reply(Optional.empty());
            } else {
                ctx.reply(Optional.of(state().get().getStatus()));
            }
        });
    }
*/
    /* Ignore duplicate submissions of an order, provided the details are the same. Duplicates can happen due to
     * at-least-once handling of the order placement process.
     */
    /*
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
    */

    private interface OrderBehaviorBuilder {
        Behavior getBehavior();
    }

    private abstract class OrderBehaviourBuilder<State> implements OrderBehaviorBuilder {

        public String entityId() { return OrderEntity.this.entityId(); }
        public Logger getLogger() { return OrderEntity.this.log; }

        abstract OrderDetails getOrderDetails();
        abstract OrderStatus getOrderStatus();

        abstract State state(); // shadow parent's state method to provide a more specific one for this behaviour.

        Order getOrder() { return new Order(entityId(), getOrderDetails()); }

        void getStatus(OrderCommand.GetStatus cmd, ReadOnlyCommandContext ctx) {
            ctx.reply(Optional.of(getOrderStatus()));
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

        void setCommonBehavior(BehaviorBuilder builder) {
            builder.setReadOnlyCommandHandler(OrderCommand.GetStatus.class, this::getStatus);
            builder.setReadOnlyCommandHandler(OrderCommand.PlaceOrder.class, this::ignoreDuplicatePlacements);
        }
    }


    private class PendingBehaviorBuilder extends OrderBehaviourBuilder<OrderState.Pending> {
        private final OrderDetails orderDetails;

        public OrderDetails getOrderDetails() { return orderDetails; }
        public OrderStatus getOrderStatus() { return OrderStatus.Pending; }

        public OrderState.Pending state() { return new OrderState.Pending(orderDetails); };

        PendingBehaviorBuilder(OrderDetails orderDetails) {
            this.orderDetails = orderDetails;
        }

        PendingBehaviorBuilder(OrderState.Pending state) {
            this(state.getOrderDetails());
        }

        private Persist complete(OrderCommand.Complete cmd, CommandContext<Done> ctx) {
            if (cmd.getOrderResult() instanceof OrderResult.OrderCompleted) {
                OrderResult.OrderCompleted orderCompleted = (OrderResult.OrderCompleted)cmd.getOrderResult();
                return ctx.thenPersist(new OrderEvent.OrderFulfilled(getOrder(), orderCompleted.getTrade()),
                        evt -> ctx.reply(Done.getInstance()));
            } else if (cmd.getOrderResult() instanceof OrderResult.OrderFailed) {
                return ctx.thenPersist(new OrderEvent.OrderFailed(getOrder()),
                        evt -> ctx.reply(Done.getInstance()));
            } else {
                throw new IllegalStateException();
            }
        }

        private Behavior fulfilled(OrderEvent.OrderFulfilled evt) {
            return new FulfilledOrderBehaviorBuilder(orderDetails, evt.getTrade().getPrice()).getBehavior();
        }

        public Behavior getBehavior() {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.of(new OrderState.Pending(this.orderDetails)));
            setCommonBehavior(builder);

            builder.setCommandHandler(OrderCommand.Complete.class, this::complete);
            builder.setEventHandlerChangingBehavior(OrderEvent.OrderFulfilled.class, this::fulfilled);

            return builder.build();
        }

    }

    private class FulfilledOrderBehaviorBuilder extends OrderBehaviourBuilder<OrderState.Fulfilled> {
        private final OrderDetails orderDetails;

        private final BigDecimal price;

        public OrderDetails getOrderDetails() { return orderDetails; }
        public OrderStatus getOrderStatus() { return OrderStatus.Pending; }
        public String entityId() { return OrderEntity.this.entityId(); }
        public Logger getLogger() { return OrderEntity.this.log; }

        public OrderState.Fulfilled state() { return new OrderState.Fulfilled(orderDetails, this.price); }

        FulfilledOrderBehaviorBuilder(OrderDetails orderDetails, BigDecimal price) {
            this.orderDetails = orderDetails;
            this.price = price;
        }

        FulfilledOrderBehaviorBuilder(OrderState.Fulfilled state) {
            this(state.getOrderDetails(), state.getPrice());
        }

        public Behavior getBehavior() {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.of(new OrderState.Fulfilled(orderDetails, this.price)));
            setCommonBehavior(builder);

            return builder.build();
        }
    }

    private class UninitializedBehaviorBuilder {
        Behavior getBehavior() {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
            builder.setCommandHandler(OrderCommand.PlaceOrder.class, this::placeOrder);

            builder.setEventHandlerChangingBehavior(OrderEvent.ProcessingOrder.class, this::processing);
            return builder.build();
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
