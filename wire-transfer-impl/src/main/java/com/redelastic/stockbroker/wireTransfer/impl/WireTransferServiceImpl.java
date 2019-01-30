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
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferProcessor;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferRepositoryImpl;
import com.redelastic.stocktrader.TransferId;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import com.redelastic.stocktrader.wiretransfer.api.TransferRequest;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import scala.PartialFunction;

import javax.inject.Inject;
import java.util.UUID;

public class WireTransferServiceImpl implements WireTransferService {

    private final TransferRepositoryImpl transferRepository;


    @Inject
    WireTransferServiceImpl(TransferRepositoryImpl transferRepository,
                            ReadSide readSide) {
        this.transferRepository = transferRepository;
        readSide.register(TransferProcessor.class);
    }

    @Override
    public ServiceCall<Transfer, TransferId> transferFunds() {
        String uuid = UUID.randomUUID().toString();
        TransferId transferId = new TransferId(uuid);

        return transfer ->
                transferRepository
                    .get(transferId)
                    .ask(TransferCommand.RequestFunds.builder()
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
                .collect(collectEvents(
                        new PFBuilder<TransferEvent, TransferRequest>()
                        .match(TransferEvent.TransferInitiated.class, this::requestFunds)
                        .match(TransferEvent.FundsRetrieved.class, this::sendFunds)
                        .build()
                ));
    }

    private TransferRequest requestFunds(TransferEvent.TransferInitiated transferInitiatedEvent) {
        return TransferRequest.WithdrawlRequest.builder()
                .account(transferInitiatedEvent.getSource())
                .amount(transferInitiatedEvent.getAmount())
                .transferId(transferInitiatedEvent.getTransferId())
                .build();
    }

    private TransferRequest sendFunds(TransferEvent.FundsRetrieved fundsRetrieved) {
        return TransferRequest.DepositRequest.builder()
                .transferId(fundsRetrieved.getTransferId())
                .account(fundsRetrieved.getDestination())
                .amount(fundsRetrieved.getAmount())
                .build();
    }

    /**
     * Build a partial function for collect that will operate on the event part only of the event stream (passing
     * through the offsets transparently).
     * @param pf
     * @param <A>
     * @param <B>
     * @return collectEvents(pf).isDefined(a,off) iff pf.isDefined(a)
     *   && collectEvents(pf).apply(a,off) = Pair(pf.apply(a), off)
     */
    private <A,B> PartialFunction<Pair<A, Offset>, Pair<B, Offset>> collectEvents(PartialFunction<A,B> pf) {
        return pmapFirst(pf);
    }

    private <A, B, C>  PartialFunction<Pair<A,C>, Pair<B,C>> pmapFirst(PartialFunction<A,B> pf) {
        FI.TypedPredicate<Pair> isDefinedOnFirst = p -> pf.isDefinedAt((((Pair<A,C>)p).first()));
        FI.Apply<Pair, Pair<B,C>> applyOnFirst = p -> Pair.<B,C>create((pf.apply((A)p.first())), (C)p.second());
        return new PFBuilder<Pair<A,C>, Pair<B,C>>()
                .match(Pair.class, isDefinedOnFirst, applyOnFirst)
                .build();
    }


}
