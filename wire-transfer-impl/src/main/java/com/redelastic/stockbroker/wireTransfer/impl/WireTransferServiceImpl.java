package com.redelastic.stockbroker.wireTransfer.impl;

import akka.Done;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.redelastic.stocktrader.wiretransfer.api.PortfolioCreditRequest;
import com.redelastic.stocktrader.wiretransfer.api.PortfolioDebitRequest;
import com.redelastic.stocktrader.wiretransfer.api.PortfolioTransfer;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;

public class WireTransferServiceImpl implements WireTransferService {

    @Override
    public ServiceCall<PortfolioCreditRequest, Done> creditPortfolio() {
        return null;
    }

    @Override
    public ServiceCall<PortfolioDebitRequest, Done> debitPortfolio() {
        return null;
    }


    private Source<Pair<PortfolioTransfer, Offset>, ?> transferStream() {
        return Source.empty();
    }
}
