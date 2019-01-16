package com.redelastic.stocktrader.broker.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.*;

public interface BrokerService extends Service {

  ServiceCall<NotUsed, Quote> getQuote(String symbol);

  ServiceCall<NotUsed, Optional<OrderStatus>> getOrderStatus(String orderId);

  String ORDER_RESULTS_TOPIC_ID = "OrderResults";
  Topic<OrderResult> orderResults();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("broker").withCalls(
            restCall(Method.GET, "/api/quote/:symbol", this::getQuote),
            restCall(Method.GET, "/api/order/:orderId", this::getOrderStatus)
    ).withTopics(
            topic(ORDER_RESULTS_TOPIC_ID, this::orderResults)
    );
    // @formatter:on
  }
}
