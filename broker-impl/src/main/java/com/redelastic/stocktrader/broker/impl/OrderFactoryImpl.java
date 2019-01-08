package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.order.Order;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

class OrderFactoryImpl implements OrderFactory {

    private final PersistentEntityRegistry persistentEntities;

    @Inject
    OrderFactoryImpl(PersistentEntityRegistry persistentEntities) {
        this.persistentEntities = persistentEntities;
        persistentEntities.register(OrderEntity.class);
    }

    public CompletionStage<Done> placeOrder(Order order) {
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
        return persistentEntities.refFor(OrderEntity.class, order.getOrderId())
                .ask(new OrderCommand.PlaceOrder(order));

    }

    private CompletionStage<Done> createSellOrder(Order order) {
        return null;
    }
}
