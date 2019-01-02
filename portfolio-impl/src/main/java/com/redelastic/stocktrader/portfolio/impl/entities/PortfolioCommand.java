package com.redelastic.stocktrader.portfolio.impl.entities;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;
import com.redelastic.stocktrader.portfolio.impl.PortfolioState;
import lombok.Builder;
import lombok.Value;

public interface PortfolioCommand extends Jsonable {

    @Value
    class Open implements PortfolioCommand, ReplyType<Done> {
        PortfolioId portfolioId;
        NewPortfolioRequest request;
    }

    enum Liquidate implements PortfolioCommand, ReplyType<Done> {
        INSTANCE
    }

    enum GetState implements PortfolioCommand, ReplyType<PortfolioState.Open> {
        INSTANCE
    }

    @Value
    @Builder
    class BuyOrder implements PortfolioCommand, ReplyType<Done> {
        String symbol;
        int shares;
        BrokerService brokerService;
    }

    @Value
    @Builder
    class SellOrder implements PortfolioCommand, ReplyType<Done> {
        String symbol;
        int shares;
        BrokerService brokerService;
    }

}
