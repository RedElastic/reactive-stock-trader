package com.redelastic.stocktrader.broker.impl.buyOrder;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.order.Order;
import lombok.Value;

import java.math.BigDecimal;

public interface BuyOrderCommand extends Jsonable {

    @Value
    class Create implements BuyOrderCommand, ReplyType<Done> {
        Order order;
    }

    @Value
    class Fulfill implements BuyOrderCommand, ReplyType<Done> {
        Order order;
        BigDecimal sharePrice;
    }
}
