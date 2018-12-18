package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.Order;
import com.redelastic.stocktrader.broker.api.Quote;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class BrokerServiceImpl implements BrokerService {
    @Override
    public ServiceCall<Order, Done> buyStock() {
        return null;
    }

    @Override
    public ServiceCall<Order, Done> sellStock() {
        return null;
    }

    @Override
    public ServiceCall<NotUsed, Quote> getQuote(String symbol) {
        return request -> {
            Quote quote = Quote.builder()
                    .symbol(symbol)
                    .sharePrice(new BigDecimal("10.40"))
                    .build();
            return CompletableFuture.completedFuture(quote);
        };
    }
}
