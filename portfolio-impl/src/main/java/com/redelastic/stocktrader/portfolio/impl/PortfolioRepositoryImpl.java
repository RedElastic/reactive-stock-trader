package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.*;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class PortfolioRepositoryImpl implements PortfolioRepository {

    private BrokerService brokerService;

    @Inject
    public PortfolioRepositoryImpl(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @Override
    public CompletionStage<Done> open(NewPortfolioRequest request) {
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    @Override
    public CompletionStage<PortfolioView> get(PortfolioId portfolioId) {
        BigDecimal funds = new BigDecimal("100");
        LoyaltyLevel loyaltyLevel = LoyaltyLevel.BRONZE;
        PSequence<Holding> holdings = ConsPStack.empty();
        PSequence<String> symbols = ConsPStack.singleton("IBM");

        PortfolioView view = new PortfolioView(portfolioId, funds, loyaltyLevel, holdings);
        return CompletableFuture.completedFuture(view);
    }

    private CompletionStage<Map<String, BigDecimal>> getPrices(PSequence<String> symbols) {
        // TODO deal with request failures
        // TODO timeout
        // TODO add in share count multiplier logic and produce list of holdings
        ConcurrentHashMap<String, BigDecimal> priceMap = new ConcurrentHashMap<String, BigDecimal>();
        Stream<CompletionStage<Void>> requests = symbols.stream().map(symbol ->
            brokerService
                    .getQuote()
                    .invoke(symbol)
                    .thenAccept(quote -> priceMap.put(symbol, quote.getSharePrice()))
        );
        // FIXME: Deal with this
        CompletableFuture<Void>[] rs = requests.toArray(CompletableFuture[]::new);
        return CompletableFuture
                .allOf(rs)
                .thenApply(u -> priceMap);
    }

}