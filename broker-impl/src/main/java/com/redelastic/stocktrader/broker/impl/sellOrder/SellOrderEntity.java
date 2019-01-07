package com.redelastic.stocktrader.broker.impl.sellOrder;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

public class SellOrderEntity extends PersistentEntity<SellOrderCommand, SellOrderEvent, SellOrderState> {
    @Override
    public Behavior initialBehavior(Optional<SellOrderState> snapshotState) {
        return null;
    }
}
