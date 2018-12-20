package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.*;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toList;

public class PortfolioRepositoryImpl implements PortfolioRepository {

    private final BrokerService brokerService;

    @Inject
    public PortfolioRepositoryImpl(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @Override
    public CompletionStage<Done> open(NewPortfolioRequest request) {
        // TODO: Do
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    @Override
    public CompletionStage<PortfolioView> get(PortfolioId portfolioId) {
        PortfolioState portfolio = new PortfolioState(
                new BigDecimal("100"),
                LoyaltyLevel.BRONZE,
                ConsPStack.singleton(new Holding("IBM", 10))
        );
        return priceHoldings(portfolio.getHoldings())
                .thenApply(valuedHoldings ->
                        new PortfolioView(portfolioId, portfolio.getFunds(), portfolio.getLoyaltyLevel(), valuedHoldings)
                );
    }

    private CompletionStage<PSequence<ValuedHolding>> priceHoldings(PSequence<Holding> holdings) {
        // TODO deal with request failures
        // TODO timeout
        List<CompletableFuture<ValuedHolding>> requests = holdings.stream().map(valuedHolding ->
                brokerService
                        .getQuote()
                        .invoke(valuedHolding.getSymbol())
                        .thenApply(quote ->
                                new ValuedHolding(
                                        valuedHolding.getSymbol(),
                                        valuedHolding.getShareCount(),
                                        quote.getSharePrice().multiply(BigDecimal.valueOf(valuedHolding.getShareCount()))))
                        .toCompletableFuture()
        ).collect(toList());
        
        return CompletableFuture.allOf(requests.toArray(new CompletableFuture<?>[0]))
                .thenApply(done ->
                    requests.stream()
                            .map(response -> response.toCompletableFuture().join())
                        .collect(toList())
                ).thenApply(ConsPStack::from);
    }

}