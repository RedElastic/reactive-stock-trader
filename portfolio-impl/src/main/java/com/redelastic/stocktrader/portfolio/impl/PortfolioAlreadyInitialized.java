package com.redelastic.stocktrader.portfolio.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;

// TODO: Confirm best practices on custom Java exceptions for Lagom
public class PortfolioAlreadyInitialized extends RuntimeException implements Jsonable {

    @JsonCreator
    public PortfolioAlreadyInitialized(String portfolioId) {
        super(String.format("Portfolio %s already initialized.", portfolioId));
    }
}
