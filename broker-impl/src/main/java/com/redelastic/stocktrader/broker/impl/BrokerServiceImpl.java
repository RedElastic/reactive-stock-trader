package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.Order;
import com.redelastic.stocktrader.broker.api.Quote;

import javax.inject.Inject;

public class BrokerServiceImpl implements BrokerService {

    private QuoteService quoteService;

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
        return symbol -> {
            return quoteService.getQuote(symbol);
        };
    }
}
