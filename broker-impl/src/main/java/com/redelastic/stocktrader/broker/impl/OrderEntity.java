package com.redelastic.stocktrader.broker.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

public class OrderEntity extends PersistentEntity<OrderCommand, OrderEvent, OrderState> {

    @Override
    public Behavior initialBehavior(Optional<OrderState> snapshotState) {
        return null;
    }


}
