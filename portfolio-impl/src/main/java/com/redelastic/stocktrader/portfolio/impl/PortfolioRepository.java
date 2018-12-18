package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;

import java.util.concurrent.CompletableFuture;

public interface PortfolioRepository {

    CompletableFuture<Done> open(NewPortfolioRequest request);

    CompletableFuture<PortfolioView> get(PortfolioId portfolioId);

}
