package com.redelastic.stocktrader.portfolio.impl;

import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;

import java.util.concurrent.CompletionStage;

public interface PortfolioRepository {

    CompletionStage<String> open(NewPortfolioRequest request);

    Portfolio get(String portfolioId);

    Source<Pair<Order, Offset>, ?> ordersStream(AggregateEventTag<PortfolioEvent> tag, Offset offset);
}
