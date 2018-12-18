package com.redelastic.stocktrader.broker.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.call;

import akka.Done;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

public interface BrokerService extends Service {

  ServiceCall<Order, Done> buyStock();
  ServiceCall<Order, Done> sellStock();

  ServiceCall<String, Quote> getQuote();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("broker").withCalls(
        call(this::buyStock),
        call(this::sellStock),
        call(this::getQuote)
    );
    // @formatter:on
  }
}
