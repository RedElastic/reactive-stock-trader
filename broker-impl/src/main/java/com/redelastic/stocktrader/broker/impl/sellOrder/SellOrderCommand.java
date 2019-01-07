package com.redelastic.stocktrader.broker.impl.sellOrder;

import akka.Done;
import com.lightbend.lagom.serialization.Jsonable;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.redelastic.stocktrader.order.Order;
import lombok.Value;

import java.math.BigDecimal;

public interface SellOrderCommand extends Jsonable {

    @Value
    class Create implements SellOrderCommand, ReplyType<Done> {
        Order order;
    }

    @Value
    class Ready implements SellOrderCommand, ReplyType<Done> {
        Order order;
    }

    @Value
    class Fulfill implements SellOrderCommand, ReplyType<Done> {
        Order order;
        BigDecimal price;
    }

}
