package com.redelastic.stocktrader.portfolio.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.call;

import akka.Done;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.math.BigDecimal;

/**
 *
 */
public interface PortfolioService extends Service {

    ServiceCall<NewPortfolioRequest, Done> openPortfolio();

    ServiceCall<PortfolioId, Done> liquidatePortfolio();

    ServiceCall<BuyOrder, Done> buyStock();

    ServiceCall<SellOrder, Done> sellStock();

    ServiceCall<PortfolioId, PortfolioView> getPortfolio();

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
                call(this::openPortfolio),
                call(this::liquidatePortfolio),
                call(this::buyStock),
                call(this::sellStock),
                call(this::getPortfolio),
                call(this::creditFunds),
                call(this::debitFunds)
        );
        /*.withTopics(
    topic(LOYALTY_LEVEL_TOPIC_ID, this::loyaltyLevelChanges)
        .withProperty(KafkaProperties.partitionKeyStrategy(), llChange -> llChange.getPortfolioId().asString())
        */
        // @formatter:on

    }
}
