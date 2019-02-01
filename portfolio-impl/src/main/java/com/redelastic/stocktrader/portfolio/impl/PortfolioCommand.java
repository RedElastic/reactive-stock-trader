package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import com.redelastic.stocktrader.OrderId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

public interface PortfolioCommand extends Jsonable {

    @Value
    class Open implements PortfolioCommand, ReplyType<Done> {
        @NonNull String name;
    }

    enum Liquidate implements PortfolioCommand, ReplyType<Done> {
        INSTANCE
    }

    enum GetState implements PortfolioCommand, ReplyType<PortfolioState.Open> {
        INSTANCE
    }

    @Value
    @Builder
    class PlaceOrder implements PortfolioCommand, ReplyType<Done> {
        @NonNull OrderId orderId;
        @NonNull OrderDetails orderDetails;
    }

    @Value
    @Builder
    class CompleteTrade implements PortfolioCommand, ReplyType<Done> {
        @NonNull OrderId orderId;
        @NonNull Trade trade;
    }

    @Value
    @Builder
    class ReceiveFunds implements PortfolioCommand, ReplyType<Done> {
        @NonNull BigDecimal amount;
    }

    @Value
    @Builder
    class SendFunds implements PortfolioCommand, ReplyType<Done> {
        @NonNull BigDecimal amount;
    }

    @Value
    @Builder
    class AcceptRefund implements PortfolioCommand, ReplyType<Done> {
        @NonNull BigDecimal amount;
    }

    // FIXME: review this.
    @Value
    class HandleOrderFailure implements PortfolioCommand, ReplyType<Done> {
        OrderResult.Failed orderFailed;
    }

    @Value
    class ClosePortfolio implements PortfolioCommand, ReplyType<Done> {
        private ClosePortfolio() {}
        public static ClosePortfolio INSTANCE = new ClosePortfolio();
    }
}
