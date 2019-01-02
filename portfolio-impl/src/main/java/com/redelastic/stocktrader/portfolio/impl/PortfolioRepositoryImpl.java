package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.*;
import com.redelastic.stocktrader.portfolio.impl.entities.PortfolioCommand;
import com.redelastic.stocktrader.portfolio.impl.entities.PortfolioEntity;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toList;

public class PortfolioRepositoryImpl implements PortfolioRepository {

    private final BrokerService brokerService;

    private final PersistentEntityRegistry persistentEntities;

    @Inject
    public PortfolioRepositoryImpl(BrokerService brokerService,
                                   PersistentEntityRegistry persistentEntities) {
        this.brokerService = brokerService;
        this.persistentEntities = persistentEntities;
        persistentEntities.register(PortfolioEntity.class);
    }

    /**
     * Initialize a new portfolio. We first generate a new ID for it and send it a setup message. In the very unlikely
     * circumstance that the ID is already in use we'll get an exception when we send the initialize command, we should
     * retry with a new UUID.
     * @param request
     * @return
     */
    // TODO: Implement retry logic. Theoretically the chance of a collision is astronomically low *given* everything else works.
    @Override
    public CompletionStage<PortfolioId> open(NewPortfolioRequest request) {
        UUID uuid = UUID.randomUUID();
        String persistenceId = uuid.toString();
        PortfolioId portfolioId = new PortfolioId(persistenceId);
        PersistentEntityRef<PortfolioCommand> ref = persistentEntities.refFor(PortfolioEntity.class, persistenceId);
        return ref.ask(new PortfolioCommand.Open(portfolioId, request))
                .thenApply(done -> portfolioId);
    }

    @Override
    public CompletionStage<PortfolioView> get(PortfolioId portfolioId) {
//        PortfolioState.Open portfolio = new PortfolioState.Open(
//                new BigDecimal("100"),
//                LoyaltyLevel.BRONZE,
//                ConsPStack.singleton(new Holding("IBM", 10)),
//     "Dummy portfolio"
//        );
        return persistentEntities.refFor(PortfolioEntity.class, portfolioId.getId())
                .ask(PortfolioCommand.GetState.INSTANCE)
                .thenCompose(portfolio ->
                    priceHoldings(portfolio.getHoldings())
                    .thenApply(valuedHoldings ->
                        PortfolioView.builder()
                                .portfolioId(portfolioId)
                                .funds(portfolio.getFunds())
                                .loyaltyLevel(portfolio.getLoyaltyLevel())
                                .holdings(valuedHoldings)
                                .build()
                    )
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

    public PersistentEntityRef<PortfolioCommand> getRef(PortfolioId portfolioId) {
        return persistentEntities.refFor(PortfolioEntity.class, portfolioId.getId());
    }

}