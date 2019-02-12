/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.broker.impl.order;

import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.broker.api.OrderResult;

public interface OrderRepository {

    OrderModel get(OrderId orderId);

    Source<Pair<OrderResult, Offset>, ?> orderResults(AggregateEventTag<OrderEvent> tag, Offset offset);
}
