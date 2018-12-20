package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.Order;
import com.redelastic.stocktrader.broker.api.Quote;

import javax.inject.Inject;

public class BrokerServiceImpl implements BrokerService {

    private final QuoteService quoteService;

    @Inject
    public BrokerServiceImpl(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public ServiceCall<Order, Done> buyStock() {
        return null;
    }

    @Override
    public ServiceCall<Order, Done> sellStock() {
        return null;
    }

    @Override
    public ServiceCall<String, Quote> getQuote() {
        return quoteService::getQuote;
    }
}
