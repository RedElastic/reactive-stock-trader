package com.redelastic.stocktrader.broker.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

public interface BrokerService extends Service {

  ServiceCall<Order, Done> buyStock();
  ServiceCall<Order, Done> sellStock();

  ServiceCall<NotUsed, Quote> getQuote(String symbol);

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("broker").withCalls(
        restCall(Method.POST, "/api/broker/buyStock", this::buyStock),
        restCall(Method.POST, "api/broker/sellStock", this::sellStock),
        restCall(Method.GET, "/api/broker/quote/:symbol", this::getQuote)
    );
    // @formatter:on
  }
}
