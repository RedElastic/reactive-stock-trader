package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.broker.impl.buyOrder.BuyOrderCommand;
import com.redelastic.stocktrader.broker.impl.buyOrder.BuyOrderEntity;
import com.redelastic.stocktrader.order.Order;

import java.util.concurrent.CompletionStage;

class OrderFactoryImpl {

    private final PersistentEntityRegistry persistentEntities;

    OrderFactoryImpl(PersistentEntityRegistry persistentEntities) {
        this.persistentEntities = persistentEntities;
    }

    CompletionStage<Done> placeOrder(Order order) {
        switch(order.getOrderType()) {
            case BUY:
                return createBuyOrder(order);
            case SELL:
                return createSellOrder(order);
            default:
                throw new IllegalStateException();
        }
    }

    private CompletionStage<Done> createBuyOrder(Order order) {
        return persistentEntities.refFor(BuyOrderEntity.class, order.getOrderId())
                .ask(new BuyOrderCommand.Create(order));

    }

    private CompletionStage<Done> createSellOrder(Order order) {
        return null;
    }
}
