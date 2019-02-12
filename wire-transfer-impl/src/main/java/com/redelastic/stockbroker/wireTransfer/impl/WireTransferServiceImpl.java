/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stockbroker.wireTransfer.impl;

import akka.japi.Pair;
import akka.japi.pf.FI;
import akka.japi.pf.PFBuilder;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferCommand;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferEvent;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferProcess;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferRepositoryImpl;
import com.redelastic.stocktrader.TransferId;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import com.redelastic.stocktrader.wiretransfer.api.TransferRequest;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import scala.PartialFunction;

import javax.inject.Inject;

public class WireTransferServiceImpl implements WireTransferService {

    private final TransferRepositoryImpl transferRepository;


    @Inject
    WireTransferServiceImpl(TransferRepositoryImpl transferRepository,
                            ReadSide readSide) {
        this.transferRepository = transferRepository;
        readSide.register(TransferProcess.class);
    }

    @Override
    public ServiceCall<Transfer, TransferId> transferFunds() {
        TransferId transferId = TransferId.newId();

        return transfer ->
                transferRepository
                        .get(transferId)
                        .ask(TransferCommand.TransferFunds.builder()
                                .source(transfer.getSourceAccount())
                                .destination(transfer.getDestinationAccount())
                                .amount(transfer.getFunds())
                                .build()
                        )
                        .thenApply(done -> transferId);
    }

    @Override
    public Topic<Transfer> completedTransfers() {
        return TopicProducer.taggedStreamWithOffset(TransferEvent.TAG.allTags(), this::completedTransfersStream);
    }

    @Override
    public Topic<TransferRequest> transferRequest() {

        return TopicProducer.taggedStreamWithOffset(TransferEvent.TAG.allTags(), this::transferRequestSource);
    }


    private Source<Pair<Transfer, Offset>, ?> completedTransfersStream(AggregateEventTag<TransferEvent> tag, Offset offset) {
        return Source.empty(); // TODO
    }

    private Source<Pair<TransferRequest, Offset>, ?> transferRequestSource(AggregateEventTag<TransferEvent> tag, Offset offset) {
        return transferRepository
                .eventStream(tag, offset)
                .collect(collectByEvent(
                        new PFBuilder<TransferEvent, TransferRequest>()
                                .match(TransferEvent.TransferInitiated.class, this::requestFunds)
                                .match(TransferEvent.FundsRetrieved.class, this::sendFunds)
                                .build()
                ));
    }

    private TransferRequest requestFunds(TransferEvent.TransferInitiated transferInitiatedEvent) {
        return TransferRequest.WithdrawlRequest.builder()
                .account(transferInitiatedEvent.getTransferDetails().getSource())
                .amount(transferInitiatedEvent.getTransferDetails().getAmount())
                .transferId(transferInitiatedEvent.getTransferId())
                .build();
    }

    private TransferRequest sendFunds(TransferEvent.FundsRetrieved fundsRetrieved) {
        return TransferRequest.DepositRequest.builder()
                .transferId(fundsRetrieved.getTransferId())
                .account(fundsRetrieved.getTransferDetails().getDestination())
                .amount(fundsRetrieved.getTransferDetails().getAmount())
                .build();
    }

    /**
     * Build a partial function for collect that will operate on the event part only of the event stream (passing
     * through the offsets transparently).
     *
     * @param pf
     * @param <A>
     * @param <B>
     * @return collectByEvent(pf).isDefined(a, off) iff pf.isDefined(a)
     * && collectByEvent(pf).apply(a,off) = Pair(pf.apply(a), off)
     */
    private <A, B> PartialFunction<Pair<A, Offset>, Pair<B, Offset>> collectByEvent(PartialFunction<A, B> pf) {
        return collectFirst(pf);
    }

    @SuppressWarnings("unchecked")
    private <A, B, C> PartialFunction<Pair<A, C>, Pair<B, C>> collectFirst(PartialFunction<A, B> pf) {
        FI.TypedPredicate<Pair> isDefinedOnFirst = p -> pf.isDefinedAt((((Pair<A, C>) p).first()));
        FI.Apply<Pair, Pair<B, C>> applyOnFirst = p -> Pair.create((pf.apply((A) p.first())), (C) p.second());
        return new PFBuilder<Pair<A, C>, Pair<B, C>>()
                .match(Pair.class, isDefinedOnFirst, applyOnFirst)
                .build();
    }


}
