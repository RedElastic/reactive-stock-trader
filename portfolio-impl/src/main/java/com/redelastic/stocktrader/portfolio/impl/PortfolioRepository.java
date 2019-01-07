package com.redelastic.stocktrader.portfolio.impl;

import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;

import java.util.concurrent.CompletionStage;

public interface PortfolioRepository {

    CompletionStage<String> open(NewPortfolioRequest request);

    CompletionStage<PortfolioView> get(String portfolioId);

    PersistentEntityRef<PortfolioCommand> getRef(String portfolioId);

    Source<Pair<Order, Offset>, ?> ordersStream(AggregateEventTag<PortfolioEvent> tag, Offset offset);
}
