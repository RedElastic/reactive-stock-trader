package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;
import lombok.Value;

public interface PortfolioCommand extends Jsonable {

    enum GetView implements PortfolioCommand, ReplyType<PortfolioView> {
        INSTANCE
    }

    @Value
    final class BuyOrder implements PortfolioCommand, ReplyType<Done> {
        final String symbol;
        final int shares;
    }

    @Value
    final class SellOrder implements PortfolioCommand, ReplyType<Done> {
        final String symbol;
        final int shares;
    }

    enum LiquidateOrder implements PortfolioCommand, ReplyType<Done> {
        INSTANCE
    }



}
