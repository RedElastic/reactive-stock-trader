package com.redelastic.wiretransfer.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static com.lightbend.lagom.javadsl.api.Service.topic;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;

public interface BrokerService extends Service {

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("broker").withCalls(
        ).withAutoAcl(true);
    // @formatter:on
  }
}
