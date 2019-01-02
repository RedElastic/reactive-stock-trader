package com.redelastic.stocktrader.portfolio.impl.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;

import com.redelastic.stocktrader.portfolio.api.PortfolioId;

// TODO: Confirm best practices on custom Java exceptions for Lagom
public class PortfolioAlreadyInitialized extends RuntimeException implements Jsonable {
    PortfolioId portfolioId;

    @JsonCreator
    public PortfolioAlreadyInitialized(PortfolioId portfolioId) {
        super(String.format("Portfolio %s already initialized.", portfolioId.getId()));
        this.portfolioId = portfolioId;
    }
}
