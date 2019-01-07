package com.redelastic.stocktrader.broker.impl.buyOrder;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.broker.impl.buyOrder.BuyOrderState.Status;

import java.util.Optional;
import java.util.function.Function;

// TODO: Add failed terminal state for orders that timeout or otherwise fail.
public class BuyOrderEntity extends PersistentEntity<BuyOrderCommand, BuyOrderEvent, Optional<BuyOrderState>> {

    @Override
    public Behavior initialBehavior(Optional<Optional<BuyOrderState>> snapshotState) {
        return snapshotState.flatMap(Function.identity())
                .map(orderState -> {
                    switch(orderState.getStatus()) {
                        case Ready:
                            return ready(orderState);
                        case Fulfilled:
                            return fulfilled(orderState);
                        default:
                            throw new IllegalStateException("Unknown status: " + orderState.getStatus());
                    }
                }).orElse(uninitialized());
    }

    private Behavior uninitialized() {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
        builder.setCommandHandler(BuyOrderCommand.Create.class, (cmd, ctx) ->
                ctx.thenPersist(new BuyOrderEvent.Ready(entityId(), cmd.getOrder()), evt ->
                        ctx.reply(Done.getInstance())));
        builder.setEventHandlerChangingBehavior(BuyOrderEvent.Ready.class, evt ->
                ready(new BuyOrderState(evt.getOrder(), Status.Ready)));
        return builder.build();
    }

    private Behavior ready(BuyOrderState initialState) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(initialState));
        builder.setCommandHandler(BuyOrderCommand.Fulfill.class, (cmd, ctx) -> {
            BuyOrderEvent.Fulfilled fulfilled = new BuyOrderEvent.Fulfilled(entityId(), state().get().getOrder());
            return ctx.thenPersist(fulfilled, evt -> ctx.reply(Done.getInstance()));
        });
        builder.setEventHandlerChangingBehavior(BuyOrderEvent.Fulfilled.class, evt ->
                fulfilled(state().get().withStatus(Status.Fulfilled)));
        return builder.build();
    }

    private Behavior fulfilled(BuyOrderState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state));
        return builder.build();
    }
}
