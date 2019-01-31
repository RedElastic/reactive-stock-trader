package com.redelastic.stocktrader.portfolio.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.PortfolioId;

// TODO: Confirm best practices on custom Java exceptions for Lagom
class PortfolioAlreadyOpened extends RuntimeException implements Jsonable {

    @JsonCreator
    public PortfolioAlreadyOpened(PortfolioId portfolioId) {
        super(String.format("PortfolioModel %s already initialized.", portfolioId));
    }
}
