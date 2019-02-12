/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.portfolio.impl;

import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import com.redelastic.stocktrader.portfolio.api.OrderPlaced;

import java.util.concurrent.CompletionStage;

public interface PortfolioRepository {

    CompletionStage<PortfolioId> open(OpenPortfolioDetails request);

    PortfolioModel get(PortfolioId portfolioId);

    PersistentEntityRef<PortfolioCommand> getRef(PortfolioId portfolioId);

    Source<Pair<OrderPlaced, Offset>, ?> ordersStream(AggregateEventTag<PortfolioEvent> tag, Offset offset);

}
