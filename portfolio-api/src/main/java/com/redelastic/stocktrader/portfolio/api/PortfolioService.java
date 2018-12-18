package com.redelastic.stocktrader.portfolio.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.math.BigDecimal;

/**
 *
 */
public interface PortfolioService extends Service {

    ServiceCall<NewPortfolioRequest, Done> openPortfolio();

    ServiceCall<NotUsed, Done> liquidatePortfolio(String portfolioId);

    ServiceCall<BuyOrder, Done> buyStock(String portfolioId);

    ServiceCall<SellOrder, Done> sellStock(String portfolioId);

    ServiceCall<NotUsed, PortfolioView> getPortfolio(String portfolioId);


    /**
     * Used by the wire transfer service to deposit a transfer.
     */
    ServiceCall<BigDecimal, Done> creditFunds();

    /**
     * Used by the wire transfer service to attempt to transfer funds out of the portfolio.
     */
    ServiceCall<BigDecimal, DebitResponse> debitFunds();


    /*
    String LOYALTY_LEVEL_TOPIC_ID = "LoyaltyLevelChanges";
    Topic<LoyaltyLevelChange> loyaltyLevelChanges();
    */

    @Override
    default Descriptor descriptor() {
        // Map with auto ACL to provide access to this API through the Lagom gateway

        // @formatter:off
        return named("portfolio").withCalls(
                // Use restCall to make it explicit that this is an ordinary HTTP endpoint
                restCall(Method.POST, "/api/portfolio", this::openPortfolio),
                restCall(Method.DELETE, "/api/portfolio/:portfolioId", this::liquidatePortfolio),
                restCall(Method.POST, "/api/portfolio/:portfolioId/buyStock", this::buyStock),
                restCall(Method.POST, "/api/portfolio/:portfolioId/sellStock", this::sellStock),
                restCall(Method.GET, "/api/portfolio/:portfolioId", this::getPortfolio),
                restCall(Method.POST, "/api/portfolio/:portfolioId/creditFunds", this::creditFunds),
                restCall(Method.POST, "/api/portfolio/:portfolioId/debitFunds", this::debitFunds)
        );
        /*.withTopics(
    topic(LOYALTY_LEVEL_TOPIC_ID, this::loyaltyLevelChanges)
        .withProperty(KafkaProperties.partitionKeyStrategy(), llChange -> llChange.getPortfolioId().asString())
        */
        // @formatter:on

    }
}
