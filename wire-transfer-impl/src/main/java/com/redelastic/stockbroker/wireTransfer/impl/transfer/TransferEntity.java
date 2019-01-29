package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.TransferId;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import lombok.extern.log4j.Log4j;

import java.util.Optional;
import java.util.function.Function;

@Log4j
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
                        case SendingFundsFailed:
                            return null; // TODO
                        case GettingFundsFailed:
                            return null; // TODO
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
        log.info("empty");
        BehaviorBuilder builder = newBehaviorBuilder(Optional.empty());
        builder.setCommandHandler(TransferCommand.RequestFunds.class, (cmd, ctx) ->
            ctx.thenPersist(
                    new TransferEvent.RequestFunds(getTransferId() ,cmd.getSource(), cmd.getDestination(), cmd.getAmount()),
                    evt -> ctx.reply(Done.getInstance())
            ));

        builder.setEventHandlerChangingBehavior(TransferEvent.RequestFunds.class, this::gettingFunds);
        return builder.build();
    }

    private Behavior gettingFunds(TransferEvent.RequestFunds evt) {
        TransferState state = new TransferState(evt.getSource(), evt.getDestination(), evt.getAmount(), TransferState.Status.GettingFunds);
        return gettingFunds(state);
    }

    private Behavior gettingFunds(TransferState state) {
        log.info("gettingFunds");
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

    private Behavior sendingFunds(TransferState state) {
        log.info("sendingFunds");
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.SendingFunds)));
        builder.setCommandHandler(TransferCommand.SendFunds.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.FundsSent(
                                getTransferId(), state().get().getSource(), state().get().getDestination(), state().get().getAmount()
                        ),
                        evt -> ctx.reply(Done.getInstance())
                ));
        builder.setCommandHandler(TransferCommand.RequestFundsSucessful.class, this::ignore);
        builder.setEventHandlerChangingBehavior(TransferEvent.FundsSent.class,
                evt -> confirmingDelivery(state));
        return builder.build();
    }

    private Behavior confirmingDelivery(TransferState state) {
        log.info("confirmingDelivery");
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

        builder.setCommandHandler(TransferCommand.RequestFundsSucessful.class, this::ignore);
        builder.setCommandHandler(TransferCommand.SendFunds.class, this::ignore);
        return builder.build();
    }

    private Behavior completed(TransferState state) {
        log.info("completed");
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.Completed)));

        builder.setCommandHandler(TransferCommand.RequestFundsSucessful.class, this::ignore);
        builder.setCommandHandler(TransferCommand.SendFundsSuccessful.class, this::ignore);
        builder.setCommandHandler(TransferCommand.SendFunds.class, this::ignore);
        return builder.build();
    }

    private <C extends TransferCommand> Persist ignore(C cmd, CommandContext<Done> ctx) {
        log.info(String.format("Ignoring command %s", cmd.toString()));
        ctx.reply(Done.getInstance());
        return ctx.done();
    }

}
