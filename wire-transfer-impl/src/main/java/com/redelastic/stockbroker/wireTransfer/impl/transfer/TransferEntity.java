package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.TransferId;

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
                        case ConfirmingDelivery:
                            return confirmingDelivery(state);
                        default:
                            // cases should be exhaustive
                            throw new IllegalStateException();
                    }
                }).orElse(empty());
    }

    private TransferId getTransferId() {
        return new TransferId(entityId());
    }

    private Behavior empty() {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
        builder.setCommandHandler(TransferCommand.RequestFunds.class, (cmd, ctx) ->
            ctx.thenPersist(
                    new TransferEvent.RequestFunds(getTransferId() ,cmd.getSource(), cmd.getDestination(), cmd.getAmount()),
                    evt -> ctx.reply(Done.getInstance())
            ));
        builder.setEventHandlerChangingBehavior(TransferEvent.RequestFunds.class, this::gettingFunds);
        return builder.build();
    }

    private Behavior gettingFunds(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.GettingFunds)));
        builder.setCommandHandler(TransferCommand.RequestFundsSucessful.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.FundsReceived(
                                getTransferId(), state().get().getSource(), state().get().getDestination(), state().get().getAmount()
                        ),
                        evt -> ctx.reply(Done.getInstance())
                ));
        builder.setEventHandlerChangingBehavior(TransferEvent.FundsReceived.class,
                evt -> sendingFunds(state));

        return builder.build();
    }

    private Behavior gettingFunds(TransferEvent.RequestFunds evt) {
        TransferState state = new TransferState(evt.getSource(), evt.getDestination(), evt.getAmount(), TransferState.Status.GettingFunds);
        return gettingFunds(state);
    }

    private Behavior sendingFunds(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.SendingFunds)));
        builder.setCommandHandler(TransferCommand.SendFunds.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.FundsSent(
                                getTransferId(), state().get().getSource(), state().get().getDestination(), state().get().getAmount()
                        ),
                        evt -> ctx.reply(Done.getInstance())
                ));
        builder.setEventHandlerChangingBehavior(TransferEvent.FundsSent.class,
                evt -> confirmingDelivery(state));
        return builder.build();
    }

    private Behavior confirmingDelivery(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.ConfirmingDelivery)));
        builder.setCommandHandler(TransferCommand.SendFundsSuccessful.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.SendConfirmed(
                                getTransferId(), state().get().getSource(), state().get().getDestination(), state().get().getAmount()
                        ),
                        evt -> ctx.reply(Done.getInstance())
                ));
        builder.setEventHandlerChangingBehavior(TransferEvent.SendConfirmed.class,
                evt -> completed(state));

        return builder.build();
    }

    private Behavior completed(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.Completed)));
        return builder.build();
    }

}
