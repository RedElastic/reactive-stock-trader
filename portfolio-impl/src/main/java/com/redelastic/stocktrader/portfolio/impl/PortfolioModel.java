package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.CSHelper;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.Quote;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.Holding;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;
import com.redelastic.stocktrader.portfolio.api.ValuedHolding;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toList;

/* Facade for a PortfolioModel. Wraps up all the logic surrounding an individual PortfolioEntity.
 * The PersistentEntity class itself can get large, so this wrapper can hold some of the logic around interactions with
 * the entity.
 */
class PortfolioModel {

    private final PersistentEntityRef<PortfolioCommand> portfolioEntity;
    private final BrokerService brokerService;
    private final String portfolioId;

    PortfolioModel(BrokerService brokerService,
                   PersistentEntityRegistry registry,
                   String portfolioId) {
        this.portfolioEntity = registry.refFor(PortfolioEntity.class, portfolioId);
        this.brokerService = brokerService;
        this.portfolioId = portfolioId;
    }

    CompletionStage<PortfolioView> view() {
        return portfolioEntity
                .ask(PortfolioCommand.GetState.INSTANCE)
                .thenApply(portfolio ->
                        PortfolioView.builder()
                                .portfolioId(portfolioId)
                                .name(portfolio.getName())
                                .funds(portfolio.getFunds())
                                .holdings(portfolio.getHoldings().asSequence())
                                .build()
                );
    }

    private CompletionStage<PSequence<ValuedHolding>> priceHoldings(PSequence<Holding> holdings) {
        List<CompletableFuture<ValuedHolding>> requests = holdings.stream()
                .map(valuedHolding -> {
                    CompletionStage<BigDecimal> getSharePrice = brokerService
                            .getQuote(valuedHolding.getSymbol())
                            .invoke()
                            .thenApply(Quote::getSharePrice);

                    CompletionStage<BigDecimal> nullPriceOnFailure = CSHelper.recover(getSharePrice, RuntimeException.class, ex -> null);

                    return nullPriceOnFailure

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

        return CSHelper.allOf(requests).thenApply(ConsPStack::from);
    }

    CompletionStage<Done> placeOrder(String orderId, OrderDetails orderDetails) {
        return portfolioEntity.ask(new PortfolioCommand.PlaceOrder(orderId,  orderDetails));
    }

    CompletionStage<Done> processTrade(Trade trade) {
        return portfolioEntity.ask(new PortfolioCommand.CompleteTrade(trade));
    }

    CompletionStage<Done> orderFailed(OrderResult.OrderFailed failed) {
        return portfolioEntity.ask(new PortfolioCommand.HandleOrderFailure(failed));
    }
}
