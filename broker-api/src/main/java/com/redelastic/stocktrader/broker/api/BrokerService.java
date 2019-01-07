package com.redelastic.stocktrader.broker.api;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;

import static com.lightbend.lagom.javadsl.api.Service.call;
import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.topic;

public interface BrokerService extends Service {

  ServiceCall<String, Quote> getQuote();

  String ORDER_RESULTS_TOPIC_ID = "OrderResults";
  Topic<OrderResult> orderResults();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("broker").withCalls(
        call(this::getQuote)
    ).withTopics(
            topic(ORDER_RESULTS_TOPIC_ID, this::orderResults)
    );
    // @formatter:on
  }
}
