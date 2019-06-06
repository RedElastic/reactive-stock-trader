/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.redelastic.stocktrader.TransferId;
import lombok.extern.log4j.Log4j;

import com.redelastic.stocktrader.wiretransfer.api.TransferCompleted;

import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Log4j
public class TransferEntity extends PersistentEntity<TransferCommand, TransferEvent, Optional<TransferState>> {

    private final PubSubRef<JsonNode> publishedTopic;

    @Inject
    public TransferEntity(PubSubRegistry pubSub) {
        publishedTopic = pubSub.refFor(TopicId.of(JsonNode.class, "transfer"));
    }

    @Override
    public Behavior initialBehavior(Optional<Optional<TransferState>> snapshotState) {
        return snapshotState
                .flatMap(Function.identity())
                .map(state -> {
                    switch (state.getStatus()) {
                        case FundsRequested:
                            return fundsRequested(state);
                        case FundsSent:
                            return sendingFunds(state);
                        case UnableToSecureFunds:
                            return fundsRequestFailed(state);
                        case RefundSent:
                            return refundSent(state);
                        case RefundDelivered:
                            return refundDelivered(state);
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
        builder.setCommandHandler(TransferCommand.TransferFunds.class, (cmd, ctx) -> {
            TransferDetails transferDetails = TransferDetails.builder()
                    .source(cmd.getSource())
                    .destination(cmd.getDestination())
                    .amount(cmd.getAmount())
                    .build();
            return ctx.thenPersist(
                    new TransferEvent.TransferInitiated(getTransferId(), transferDetails),
                    evt -> ctx.reply(Done.getInstance()));
        });

        builder.setEventHandlerChangingBehavior(TransferEvent.TransferInitiated.class, this::fundsRequested);
        return builder.build();
    }

    private Behavior fundsRequested(TransferEvent.TransferInitiated evt) {
        TransferState state = TransferState.from(evt.getTransferDetails());
        return fundsRequested(state);
    }

    private Behavior fundsRequested(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.FundsRequested)));
        builder.setCommandHandler(TransferCommand.RequestFundsSuccessful.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.FundsRetrieved(
                                getTransferId(),
                                state().get().getTransferDetails()
                        ),
                        evt -> ctx.reply(Done.getInstance())
                ));
        builder.setEventHandlerChangingBehavior(TransferEvent.FundsRetrieved.class,
                evt -> sendingFunds(state().get()));
        builder.setCommandHandler(TransferCommand.RequestFundsFailed.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.CouldNotSecureFunds(
                                getTransferId(),
                                state().get().getTransferDetails()
                        ),
                        evt -> ctx.reply(Done.getInstance())
                ));
        builder.setEventHandlerChangingBehavior(TransferEvent.CouldNotSecureFunds.class,
                evt -> fundsRequestFailed(state().get()));

        return builder.build();
    }

    private Behavior fundsRequestFailed(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.UnableToSecureFunds)));
        builder.setCommandHandler(TransferCommand.RequestFundsFailed.class, this::ignore);
        return builder.build();
    }

    private Behavior sendingFunds(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.FundsSent)));
        
        builder.setCommandHandler(TransferCommand.DeliverySuccessful.class, (cmd, ctx) -> {                
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();

                TransferCompleted transferCompletedEvent = TransferCompleted.builder()
                    .id(getTransferId().toString())
                    .status("Delivery Confirmed")
                    .dateTime(dateFormat.format(date))
                    .destination(state.transferDetails.destination.toString())
                    .source(state.transferDetails.source.toString())
                    .amount(state.transferDetails.amount.toString())
                    .build();

                ObjectMapper mapper = new ObjectMapper();
                publishedTopic.publish(mapper.valueToTree(transferCompletedEvent)); 

                return ctx.thenPersist(
                    new TransferEvent.DeliveryConfirmed(
                            getTransferId(),
                            state().get().getTransferDetails()
                    ),
                    evt -> ctx.reply(Done.getInstance())
                );
            }
        );

        builder.setEventHandlerChangingBehavior(TransferEvent.DeliveryConfirmed.class,
                evt -> deliveryConfirmed(state));

        builder.setCommandHandler(TransferCommand.DeliveryFailed.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.DeliveryFailed(getTransferId(), state().get().getTransferDetails()),
                        ect -> ctx.reply(Done.getInstance())
                ));

        builder.setEventHandlerChangingBehavior(TransferEvent.DeliveryFailed.class,
                evt -> refundSent(state));

        builder.setCommandHandler(TransferCommand.RequestFundsSuccessful.class, this::ignore);
        
        return builder.build();
    }

    private Behavior deliveryConfirmed(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.DeliveryConfirmed)));

        builder.setCommandHandler(TransferCommand.RequestFundsSuccessful.class, this::ignore);
        builder.setCommandHandler(TransferCommand.DeliverySuccessful.class, this::ignore);               

        return builder.build();
    }

    private Behavior refundSent(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.RefundSent)));
        builder.setCommandHandler(TransferCommand.RefundSuccessful.class, (cmd, ctx) ->
                ctx.thenPersist(
                        new TransferEvent.RefundDelivered(
                                getTransferId(),
                                state().get().getTransferDetails()
                        )
                ));
        builder.setEventHandlerChangingBehavior(TransferEvent.RefundDelivered.class, evt -> refundDelivered(state));

        builder.setCommandHandler(TransferCommand.RequestFundsSuccessful.class, this::ignore);
        return builder.build();
    }

    private Behavior refundDelivered(TransferState state) {
        BehaviorBuilder builder = newBehaviorBuilder(Optional.of(state.withStatus(TransferState.Status.RefundDelivered)));

        builder.setCommandHandler(TransferCommand.RequestFundsSuccessful.class, this::ignore);
        return builder.build();
    }

    private <C extends TransferCommand> Persist ignore(C cmd, CommandContext<Done> ctx) {
        log.info(String.format("Ignoring command %s in state %s", cmd.toString(), state().toString()));
        ctx.reply(Done.getInstance());
        return ctx.done();
    }

}
