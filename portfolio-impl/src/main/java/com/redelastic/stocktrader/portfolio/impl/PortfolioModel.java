package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import org.pcollections.ConsPStack;

import java.util.concurrent.CompletionStage;

/* Facade for a PortfolioModel. Wraps up all the logic surrounding an individual PortfolioEntity.
 * The PersistentEntity class itself can get large, so this wrapper can hold some of the logic around interactions with
 * the entity.
 */
class PortfolioModel {

    private final PersistentEntityRef<PortfolioCommand> portfolioEntity;
    private final PortfolioId portfolioId;

    PortfolioModel(PersistentEntityRegistry registry,
                   PortfolioId portfolioId) {
        this.portfolioEntity = registry.refFor(PortfolioEntity.class, portfolioId.getId());
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
                                .completedOrders(ConsPStack.from(portfolio.getCompletedOrders()))
                                .build()
                );
    }

    CompletionStage<Done> placeOrder(OrderId orderId, OrderDetails orderDetails) {
        return portfolioEntity.ask(new PortfolioCommand.PlaceOrder(orderId, orderDetails));
    }

    CompletionStage<Done> processTrade(OrderId orderId, Trade trade) {
        return portfolioEntity.ask(new PortfolioCommand.CompleteTrade(orderId, trade));
    }

    CompletionStage<Done> orderFailed(OrderResult.Failed failed) {
        return portfolioEntity.ask(new PortfolioCommand.AcknowledgeOrderFailure(failed));
    }
}
