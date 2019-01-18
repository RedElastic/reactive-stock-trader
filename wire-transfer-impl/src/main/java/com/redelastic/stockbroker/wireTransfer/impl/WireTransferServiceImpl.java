package com.redelastic.stockbroker.wireTransfer.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
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

    @Override
    public Topic<PortfolioTransfer> portfolioTransfer() {
        return null;
    }
}
