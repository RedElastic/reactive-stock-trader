package com.redelastic.stockbroker.wireTransfer.impl;

import akka.Done;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferEvent;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferRepository;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import com.redelastic.stocktrader.wiretransfer.api.TransferRequest;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;

import javax.inject.Inject;

public class WireTransferServiceImpl implements WireTransferService {

    private final TransferRepository transferRepository;

    @Inject
    WireTransferServiceImpl(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @Override
    public ServiceCall<Transfer, Done> transferFunds() {
        return null;
    }

    @Override
    public Topic<Transfer> completedTransfers() {
        return TopicProducer.taggedStreamWithOffset(TransferEvent.TAG.allTags(), this::completedTransfersStream);
    }

    @Override
    public Topic<TransferRequest> transferRequests() {
        return null;
    }


    private Source<Pair<Transfer, Offset>, ?> completedTransfersStream(AggregateEventTag<TransferEvent> tag, Offset offset) {
        return Source.empty(); // TODO
    }

}
