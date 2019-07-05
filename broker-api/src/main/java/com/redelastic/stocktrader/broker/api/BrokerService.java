/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.broker.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.redelastic.stocktrader.OrderId;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.*;

public interface BrokerService extends Service {

    String ORDER_RESULTS_TOPIC_ID = "Broker-OrderResults";

    /**
     * Get the most recent share sharePrice for a stock.
     *
     * @param symbol Stock ticker symbol.
     * @return
     */
    ServiceCall<NotUsed, Quote> getQuote(String symbol);

    /**
     * Get the current status of an order.
     *
     * @param orderId ID for the order.
     * @return Status of the order, if it exists, empty if no such order ID is known.
     */
    ServiceCall<NotUsed, Optional<OrderSummary>> getOrderSummary(OrderId orderId);

    /**
     * Completion events for orders, either successfully as a trade, or unsuccessfully (due to expiration of timeout
     * or otherwise).
     */
    Topic<OrderResult> orderResult();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("reactivestock-broker").withCalls(
                restCall(Method.GET, "/api/quote/:symbol", this::getQuote),
                restCall(Method.GET, "/api/order/:orderId", this::getOrderSummary)
        ).withTopics(
                topic(ORDER_RESULTS_TOPIC_ID, this::orderResult)
                        .withProperty(KafkaProperties.partitionKeyStrategy(), orderResult -> orderResult.getPortfolioId().getId())
        ).withPathParamSerializer(OrderId.class, OrderId.pathParamSerializer);
        // @formatter:on
    }
}
