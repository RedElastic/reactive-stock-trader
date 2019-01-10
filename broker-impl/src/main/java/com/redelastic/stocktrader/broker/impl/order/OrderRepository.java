package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;

import java.util.concurrent.CompletionStage;

public interface OrderRepository {

    OrderProcessImpl get(String orderId);

    CompletionStage<Done> placeOrder(Order order);

    Source<Pair<OrderResult, Offset>, ?> orderResults(AggregateEventTag<OrderEvent> tag, Offset offset);
}
