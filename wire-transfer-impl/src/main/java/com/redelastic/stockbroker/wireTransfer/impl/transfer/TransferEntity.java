package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;
import java.util.function.Function;


public class TransferEntity extends PersistentEntity<TransferCommand, TransferEvent, Optional<TransferState>> {

    @Override
    public Behavior initialBehavior(Optional<Optional<TransferState>> snapshotState) {
        return snapshotState
                .flatMap(Function.identity())
                .map(state -> {
                    switch (state.getStatus()) {
                        case GettingFunds:
                            return gettingFunds(state);
                        case SendingFunds:
                            return sendingFunds(state);
                        case Completed:
                            return completed(state);
                        default:
                            throw new IllegalStateException();
                    }
                }).orElse(empty());
    }

    private Behavior empty() {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
        builder.setCommandHandler(TransferCommand.Start.class, (cmd, ctx) ->
            ctx.thenPersist(
                    new TransferEvent.Started(entityId(),cmd.getSource(), cmd.getDestination(), cmd.getAmount()),
                    evt -> ctx.reply(Done.getInstance())
            ));
        builder.setEventHandlerChangingBehavior(TransferEvent.Started.class, this::gettingFunds);
        return builder.build();
    }

    private Behavior gettingFunds(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.GettingFunds)));
        builder.setCommandHandler(TransferCommand.ReceiveFunds.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.FundsReceived(
                                entityId(), state().get().getSource(), state().get().getDestination(), state().get().getAmount()
                        ),
                        evt -> ctx.reply(Done.getInstance())
                ));
        builder.setEventHandlerChangingBehavior(TransferEvent.FundsReceived.class,
                evt -> sendingFunds(state));

        return builder.build();
    }

    private Behavior gettingFunds(TransferEvent.Started evt) {
        TransferState state = new TransferState(evt.getSource(), evt.getDestination(), evt.getAmount(), TransferState.Status.GettingFunds);
        return gettingFunds(state);
    }

    private Behavior sendingFunds(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.SendingFunds)));
        builder.setCommandHandler(TransferCommand.SendFunds.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.FundsSent(
                                entityId(), state().get().getSource(), state().get().getDestination(), state().get().getAmount()
                        ),
                        evt -> ctx.reply(Done.getInstance())
                ));
        builder.setEventHandlerChangingBehavior(TransferEvent.FundsSent.class,
                evt -> completed(state));
        return builder.build();
    }

    private Behavior completed(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.Completed)));

        return builder.build();
    }

}
