package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.Quote;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;
import com.redelastic.stocktrader.portfolio.api.ValuedHolding;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/* Facade for a Portfolio. Wraps up all the logic surrounding an individual PortfolioEntity.
 * The PersistentEntity class itself can get large, so this wrapper can hold some of the logic around interactions with
 * the entity.
 */
class Portfolio {

    private final PersistentEntityRef<PortfolioCommand> portfolioEntity;
    private final BrokerService brokerService;
    private final String portfolioId;

    Portfolio(BrokerService brokerService,
              PersistentEntityRegistry registry,
              String portfolioId) {
        this.portfolioEntity = registry.refFor(PortfolioEntity.class, portfolioId);
        this.brokerService = brokerService;
        this.portfolioId = portfolioId;
    }

    CompletionStage<PortfolioView> view() {
        return portfolioEntity
                .ask(PortfolioCommand.GetState.INSTANCE)
                .thenCompose(portfolio ->
                        priceHoldings(portfolio.getHoldings().asSequence())
                                .thenApply(valuedHoldings ->
                                        PortfolioView.builder()
                                                .portfolioId(portfolioId)
                                                .name(portfolio.getName())
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
        List<CompletableFuture<ValuedHolding>> requests = holdings.stream()
                .map(valuedHolding -> {
                    CompletionStage<BigDecimal> getSharePrice = brokerService
                            .getQuote(valuedHolding.getSymbol())
                            .invoke()
                            .thenApply(Quote::getSharePrice)
                            .handle((sharePrice, ex) -> {
                                if (ex == null) {
                                    return CompletableFuture.completedFuture(sharePrice);
                                } else {
                                    CompletableFuture<BigDecimal> result = new CompletableFuture<>();
                                    if (ex instanceof RuntimeException) {
                                        result.complete(null);
                                    } else {
                                        result.completeExceptionally(ex);
                                    }
                                    return result;
                                }
                            })
                            .thenCompose(Function.identity());
                    return getSharePrice
                            .thenApply(sharePrice -> {
                                BigDecimal price = sharePrice == null ? null : sharePrice.multiply(BigDecimal.valueOf(valuedHolding.getShareCount()));
                                return new ValuedHolding(
                                        valuedHolding.getSymbol(),
                                        valuedHolding.getShareCount(),
                                        price);
                            })
                            .toCompletableFuture();
                })
                .collect(toList());

        return CompletableFuture.allOf(requests.toArray(new CompletableFuture<?>[0]))
                .thenApply(done ->
                        requests.stream()
                                .map(response -> response.toCompletableFuture().join())
                                .collect(toList())
                ).thenApply(ConsPStack::from);
    }

    CompletionStage<Done> placeOrder(Order order) {
        return portfolioEntity.ask(new PortfolioCommand.PlaceOrder(order));
    }

    CompletionStage<Done> processTrade(Trade trade) {
        return portfolioEntity.ask(new PortfolioCommand.CompleteTrade(trade));
    }

    CompletionStage<Done> orderFailed(OrderResult.OrderFailed failed) {
        return portfolioEntity.ask(new PortfolioCommand.HandleOrderFailure(failed));
    }
}
