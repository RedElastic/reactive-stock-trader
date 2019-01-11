package com.redelastic.stocktrader.portfolio.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 *
 */
public interface PortfolioService extends Service {

    ServiceCall<OpenPortfolioRequest, String> openPortfolio();

    ServiceCall<NotUsed, PortfolioView> getPortfolio(String portfolioId);

    ServiceCall<NotUsed, Done> liquidatePortfolio(String portfolioId);

    ServiceCall<OrderDetails, Done> placeOrder(String portfolioId);

    String ORDERS_TOPIC_ID = "PortfolioOrders";
    Topic<Order> orders();
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
                pathCall("/api/portfolio", this::openPortfolio),
                pathCall("/api/portfolio/:portfolioId/liquidate", this::liquidatePortfolio),
                pathCall("/api/portfolio/:portfolioId", this::getPortfolio),
                pathCall("/api/portfolio/:portfolioId/placeOrder", this::placeOrder)
        ).withTopics(
            topic(ORDERS_TOPIC_ID, this::orders)
        );
        // @formatter:on

    }
}