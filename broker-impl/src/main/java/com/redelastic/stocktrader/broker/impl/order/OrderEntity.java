package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderId;
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
                .map(orderState ->
                    orderState.visit(new OrderState.Visitor<Behavior>() {
                        @Override
                        public Behavior visit(OrderState.Pending pending) {
                            return new PendingBehaviorBuilder((OrderState.Pending) orderState).getBehavior();
                        }

                        @Override
                        public Behavior visit(OrderState.Fulfilled fulfilled) {
                            return new FulfilledOrderBehaviorBuilder((OrderState.Fulfilled) orderState).getBehavior();
                        }

                        @Override
                        public Behavior visit(OrderState.Failed failed) {
                            return new FailedOrderBehavior((OrderState.Failed) orderState).getBehavior();
                        }
                    })
                )
                .orElse(new UninitializedBehaviorBuilder().getBehavior());
    }

    /**
     * Base class for OrderBehavior covering pending and completed orderPlaced. Not this is not completely type safe, it is
     * possible to set an event handler that produces a state that doesn't correspond to the current behaviour.
     *
     * @param <State> The type of state associated to this behavior.
     */
    private abstract class OrderBehaviourBuilder<State extends OrderState> {

        Order getOrder() {
            return new Order(getOrderId(), state().getPortfolioId(), state().getOrderDetails());
        }

        @SuppressWarnings("unchecked")
        State state() {
            return (State) OrderEntity.this.state().get();
        }

        void getStatus(OrderCommand.GetStatus cmd, ReadOnlyCommandContext<Optional<OrderStatus>> ctx) {
            ctx.reply(Optional.of(state().getStatus()));
        }

        void ignoreDuplicatePlacements(OrderCommand.PlaceOrder cmd, ReadOnlyCommandContext<Order> ctx) {
            OrderDetails orderDetails = cmd.getOrderDetails();
            if (orderDetails.equals(state().getOrderDetails())) {
                ctx.reply(getOrder());
            } else {
                log.info(String.format(
                        "Order %s, existing: %s, received %s",
                        entityId(),
                        state().getOrderDetails(),
                        orderDetails.toString()
                ));
                ctx.commandFailed(new InvalidCommandException(
                        String.format("Attempt to place different order with same order ID: %s", entityId())));
            }
        }

        /**
         * Concrete behavior builders should invoke this to add the common behavior implemented here.
         *
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

            builder.setCommandHandler(OrderCommand.CompleteOrder.class, this::complete);

            builder.setEventHandlerChangingBehavior(OrderEvent.OrderFulfilled.class, this::fulfilled);
            builder.setEventHandlerChangingBehavior(OrderEvent.OrderFailed.class, this::failed);
            this.behavior = builder.build();
        }

        PendingBehaviorBuilder(PortfolioId portfolioId, OrderDetails orderDetails) {

            this(new OrderState.Pending(portfolioId, orderDetails));
        }

        private Persist complete(OrderCommand.CompleteOrder cmd, CommandContext<Done> ctx) {
            return cmd.getOrderResult().visit(new OrderResult.Visitor<Persist>() {
                @Override
                public Persist visit(OrderResult.Fulfilled orderFulfilled) {
                    return ctx.thenPersist(new OrderEvent.OrderFulfilled(getOrder(), orderFulfilled.getTrade()),
                            evt -> ctx.reply(Done.getInstance()));
                }

                @Override
                public Persist visit(OrderResult.Failed orderFailed) {
                    return ctx.thenPersist(new OrderEvent.OrderFailed(getOrder()),
                            evt -> ctx.reply(Done.getInstance()));
                }
            });
        }

        private Behavior fulfilled(OrderEvent.OrderFulfilled evt) {
            return new FulfilledOrderBehaviorBuilder(state().getPortfolioId(), state().getOrderDetails(), evt.getTrade().getPrice()).getBehavior();
        }

        private Behavior failed(OrderEvent.OrderFailed evt) {
            return new FailedOrderBehavior(evt.getOrder().getPortfolioId(), evt.getOrder().getDetails()).getBehavior();
        }

        Behavior getBehavior() {
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

        FulfilledOrderBehaviorBuilder(PortfolioId portfolioId, OrderDetails orderDetails, BigDecimal price) {
            this(new OrderState.Fulfilled(portfolioId, orderDetails, price));
        }

        Behavior getBehavior() {

            return this.behavior;
        }
    }

    private class FailedOrderBehavior extends OrderBehaviourBuilder<OrderState.Failed> {
        private final Behavior behavior;

        FailedOrderBehavior(OrderState.Failed state) {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state));
            setCommonBehavior(builder);

            this.behavior = builder.build();
        }

        FailedOrderBehavior(PortfolioId portfolioId, OrderDetails orderDetails) {
            this(new OrderState.Failed(portfolioId, orderDetails));
        }

        Behavior getBehavior() {

            return this.behavior;
        }
    }

    private class UninitializedBehaviorBuilder {
        private final Behavior behavior;

        UninitializedBehaviorBuilder() {
            BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
            builder.setCommandHandler(OrderCommand.PlaceOrder.class, this::placeOrder);
            builder.setEventHandlerChangingBehavior(OrderEvent.OrderReceived.class, this::processing);
            this.behavior = builder.build();
        }

        Behavior getBehavior() {
            return behavior;
        }

        private Persist placeOrder(OrderCommand.PlaceOrder cmd, CommandContext<Order> ctx) {
            Order order = new Order(getOrderId(), cmd.getPortfolioId(), cmd.getOrderDetails());
            return ctx.thenPersist(
                    new OrderEvent.OrderReceived(order),
                    evt -> ctx.reply(order));
        }

        private Behavior processing(OrderEvent.OrderReceived evt) {
            return new PendingBehaviorBuilder(evt.getOrder().getPortfolioId(), evt.getOrder().getDetails()).getBehavior();
        }
    }

    private OrderId getOrderId() { return new OrderId(entityId()); }


}
