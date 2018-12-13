package com.redelastic.wiretransfer.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

import akka.NotUsed;
import akka.Done;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

/**
 *
 */
public interface PortfolioService extends Service {

  ServiceCall<NewPortfolioRequest, Done> openPortfolio();

  ServiceCall<NotUsed, Done> liquidatePortfolio(String id);

  ServiceCall<ShareOrder, Done> buyStock(String portfolioId);

  ServiceCall<ShareOrder, Done> sellStock(String portfolioId);

  ServiceCall<NotUsed, PortfolioView> getPortfolio(String portfolioId);


  @Override
  default Descriptor descriptor() {
    // Map with auto ACL to provide access to this API through the Lagom gateway

    // @formatter:off
    return named("portfolio").withCalls(
        // Use restCall to make it explicit that this is an ordinary HTTP endpoint
        restCall(Method.POST,"/api/portfolio",  this::openPortfolio),
            restCall(Method.DELETE, "/api/portfolio/:id", this::liquidatePortfolio),
            restCall(Method.POST, "/api/portfolio/:id/buy", this::buyStock),
            restCall(Method.POST, "/api/portfolio/:id/sell", this::sellStock),
            restCall(Method.GET, "/api/portfolio/:id", this::getPortfolio)
      ).withAutoAcl(true);
    // @formatter:on

  }
}
