package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;

import java.util.concurrent.CompletionStage;

public interface PortfolioRepository {

    CompletionStage<Done> open(NewPortfolioRequest request);

    CompletionStage<PortfolioView> get(PortfolioId portfolioId);

}
