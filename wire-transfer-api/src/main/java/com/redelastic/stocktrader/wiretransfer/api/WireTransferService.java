package com.redelastic.stocktrader.wiretransfer.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.call;

import akka.Done;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;


/**
 * We won't try to model arbitrary account to account transfers
 */
public interface WireTransferService extends Service {

  ServiceCall<PortfolioCreditRequest, Done> creditPortfolio();

  ServiceCall<PortfolioDebitRequest, Done> debitPortfolio();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("wire-transfer").withCalls(
            call(this::creditPortfolio),
            call(this::debitPortfolio)
        ).withAutoAcl(true);
    // @formatter:on
  }
}
