package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.*;
import com.redelastic.stocktrader.portfolio.impl.entities.PortfolioCommand;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final BrokerService brokerService;

    @Inject
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository,
                                BrokerService brokerService) {
        this.portfolioRepository = portfolioRepository;
        this.brokerService = brokerService;
    }

    @Override
    public ServiceCall<NewPortfolioRequest, PortfolioId> openPortfolio() { return portfolioRepository::open; }

    @Override
    public ServiceCall<PortfolioId, Done> liquidatePortfolio() {
        return null;
    }

    @Override
    public ServiceCall<Order, Done> placeOrder() {
        return order -> {
            PortfolioId portfolioId = new PortfolioId(order.getPortfolioId());
            // TODO map order to appropriate order
            PortfolioCommand.BuyOrder orderCmd = PortfolioCommand.BuyOrder.builder()
                    .symbol(order.getStockSymbol())
                    .shares(order.getShares())
                    .brokerService(brokerService)
                    .build();
            return portfolioRepository.getRef(portfolioId)
                    .ask(orderCmd);
        };
    }


    @Override
    public ServiceCall<PortfolioId, PortfolioView> getPortfolio() { return portfolioRepository::get; }

    @Override
    public ServiceCall<BigDecimal, Done> creditFunds() {
        return null;
    }

    @Override
    public ServiceCall<BigDecimal, DebitResponse> debitFunds() {
        return null;
    }

}
