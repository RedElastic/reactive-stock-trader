package com.redelastic.stocktrader.broker.impl.order;

import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import lombok.Value;

import java.math.BigDecimal;

public interface OrderState {

    OrderDetails getOrderDetails();

    OrderStatus getStatus();

    PortfolioId getPortfolioId();

    <T> T visit(Visitor<T> visitor);

    interface Visitor<T> {
        T visit(Pending pending);

        T visit(Fulfilled fulfilled);

        T visit(Failed failed);
    }

    @Value
    class Pending implements OrderState {
        PortfolioId portfolioId;
        OrderDetails orderDetails;

        public OrderStatus getStatus() {
            return OrderStatus.Pending.INSTANCE;
        }

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Value
    class Fulfilled implements OrderState {
        PortfolioId portfolioId;
        OrderDetails orderDetails;
        BigDecimal price;

        public OrderStatus getStatus() {
            return new OrderStatus.Fulfilled(price);
        }

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    @Value
    class Failed implements OrderState {
        PortfolioId portfolioId;
        OrderDetails orderDetails;

        public OrderStatus getStatus() {
            return OrderStatus.Failed.INSTANCE;
        }

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

}