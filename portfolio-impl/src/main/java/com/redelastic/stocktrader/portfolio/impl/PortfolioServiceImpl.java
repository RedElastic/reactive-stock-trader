package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.redelastic.stocktrader.portfolio.api.*;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class PortfolioServiceImpl implements PortfolioService {

    private PortfolioRepository portfolioRepository;

    @Inject
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Override
    public ServiceCall<NewPortfolioRequest, Done> openPortfolio() {
        return newPortfolioRequest -> {
            return portfolioRepository.open(newPortfolioRequest);
        };
    }

    @Override
    public ServiceCall<NotUsed, Done> liquidatePortfolio(String portfolioId) {
        return null;
    }

    @Override
    public ServiceCall<BuyOrder, Done> buyStock(String portfolioId) {
        return null;
    }

    @Override
    public ServiceCall<SellOrder, Done> sellStock(String portfolioId) {
        return null;
    }

    @Override
    public ServiceCall<NotUsed, PortfolioView> getPortfolio(String portfolioId) {
        return request -> {
            return portfolioRepository.get(new PortfolioId(portfolioId));
        };
    }

    @Override
    public ServiceCall<BigDecimal, Done> creditFunds() {
        return null;
    }

    @Override
    public ServiceCall<BigDecimal, DebitResponse> debitFunds() {
        return null;
    }

}
