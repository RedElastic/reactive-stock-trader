package com.redelastic.stocktrader.broker.impl.order;

import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.redelastic.stocktrader.broker.api.OrderResult;

public interface OrderRepository {

    OrderModel get(String orderId);

    Source<Pair<OrderResult, Offset>, ?> orderResults(AggregateEventTag<OrderEvent> tag, Offset offset);
}
