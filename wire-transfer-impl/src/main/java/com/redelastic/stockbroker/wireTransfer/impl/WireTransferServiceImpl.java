/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stockbroker.wireTransfer.impl;

import akka.NotUsed;
import akka.japi.Pair;
import akka.japi.pf.FI;
import akka.japi.pf.PFBuilder;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferCommand;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferEvent;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferEventProcessor;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferProcess;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferRepositoryImpl;
import com.redelastic.stocktrader.TransferId;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import com.redelastic.stocktrader.wiretransfer.api.TransferCompleted;
import com.redelastic.stocktrader.wiretransfer.api.TransferRequest;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import com.redelastic.stocktrader.wiretransfer.api.TransactionSummary;
import scala.PartialFunction;
import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.List;
import java.util.function.Predicate;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

public class WireTransferServiceImpl implements WireTransferService {

    private final TransferRepositoryImpl transferRepository;
    private final CassandraSession db;
    private final PubSubRegistry pubSub;

    @Inject
    WireTransferServiceImpl(TransferRepositoryImpl transferRepository,
                            ReadSide readSide,
                            CassandraSession db,
                            PubSubRegistry pubSub) {
        this.transferRepository = transferRepository;
        this.db = db;
        this.pubSub = pubSub;
        readSide.register(TransferProcess.class);
        readSide.register(TransferEventProcessor.class);
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
    public Topic<TransferRequest> transferRequest() {
        return TopicProducer.taggedStreamWithOffset(TransferEvent.TAG.allTags(), this::transferRequestSource);
    }

    @Override
    public ServiceCall<NotUsed, PSequence<TransactionSummary>> getAllTransactionsFor(String portfolioId) {
        return request -> {
            Predicate<TransactionSummary> predicate = s -> s.source.equals(portfolioId) || s.destination.equals(portfolioId);
            CompletionStage<PSequence<TransactionSummary>> result = db.selectAll(
                "SELECT transferId, status, dateTime, source, destination, amount FROM transfer_summary;").thenApply(rows -> {
                    List<TransactionSummary> summary = rows.stream().map(row -> 
                        TransactionSummary.builder()
                            .id(row.getString("transferid"))
                            .status(row.getString("status"))
                            .dateTime(row.getString("dateTime"))
                            .source(row.getString("source"))
                            .destination(row.getString("destination"))
                            .amount(row.getString("amount"))
                            .build())                        
                        .filter(predicate)
                        .collect(Collectors.toList());
                    return TreePVector.from(summary);
                });
            return result;
        };
    }

    @Override
    public ServiceCall<NotUsed, Source<String, ?>> transferStream() {
        return request -> {
            final PubSubRef<String> topic = pubSub.refFor(TopicId.of(String.class, "transfer"));
            return CompletableFuture.completedFuture(topic.subscriber());
        };
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
