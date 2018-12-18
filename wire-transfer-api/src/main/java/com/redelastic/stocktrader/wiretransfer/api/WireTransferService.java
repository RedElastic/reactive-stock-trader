package com.redelastic.stocktrader.wiretransfer.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.Service.topic;

import akka.Done;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;


/**
 * We won't try to model arbitrary account to account transfers
 */
public interface WireTransferService extends Service {

  ServiceCall<TransferRequest, Done> transferFunds();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("wire-transfer").withCalls(
            restCall(Method.POST, "/api/wire-transfer", this::transferFunds)
        ).withAutoAcl(true);
    // @formatter:on
  }
}
