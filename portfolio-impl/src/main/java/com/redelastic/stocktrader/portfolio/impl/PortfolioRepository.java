package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;
import com.redelastic.stocktrader.portfolio.impl.entities.PortfolioCommand;

import java.util.concurrent.CompletionStage;

public interface PortfolioRepository {

    CompletionStage<PortfolioId> open(NewPortfolioRequest request);

    CompletionStage<PortfolioView> get(PortfolioId portfolioId);

    PersistentEntityRef<PortfolioCommand> getRef(PortfolioId portfolioId);
}
