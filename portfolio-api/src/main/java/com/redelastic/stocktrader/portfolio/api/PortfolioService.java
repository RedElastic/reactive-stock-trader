/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.portfolio.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 *
 */
public interface PortfolioService extends Service {

    String ORDERS_TOPIC_ID = "Portfolio-OrderPlaced";

    ServiceCall<OpenPortfolioDetails, PortfolioId> openPortfolio();

    /**
     * Place an order for a particular portfolio.
     *
     * @param portfolioId ID for the portfolio placing the order.
     * @return Order ID when the order has been accepted. For a sell order this requires confirming that the
     * requested number of shares are available to be sold.
     */
    ServiceCall<OrderDetails, OrderId> placeOrder(PortfolioId portfolioId);

    ServiceCall<NotUsed, Done> closePortfolio(PortfolioId portfolioId);

    /**
     * Get a view of the portfolio, including the current valuation of the equities held in it.
     *
     * @param portfolioId ID of the portfolio to view.
     * @return The current portfolio's state.
     */
    ServiceCall<NotUsed, PortfolioView> getPortfolio(PortfolioId portfolioId);

    ServiceCall<FundsTransfer, Done> processTransfer(PortfolioId portfolioId);

    /**
     * The orders placed by portfolios managed by this service.
     *
     * @return Orders placed by portfolios.
     */
    Topic<OrderPlaced> orderPlaced();

    @Override
    default Descriptor descriptor() {

        // @formatter:off
        return named("portfolio").withCalls(
                // Use restCall to make it explicit that this is an ordinary HTTP endpoint
                restCall(Method.POST, "/api/portfolio", this::openPortfolio),
                restCall(Method.POST, "/api/portfolio/:portfolioId/close", this::closePortfolio),
                restCall(Method.GET, "/api/portfolio/:portfolioId", this::getPortfolio),
                restCall(Method.POST, "/api/portfolio/:portfolioId/placeOrder", this::placeOrder),
                restCall(Method.POST, "/api/portfolio/:portfolio/processTransfer", this::processTransfer)
        ).withTopics(
                topic(ORDERS_TOPIC_ID, this::orderPlaced)
        ).withPathParamSerializer(PortfolioId.class, PortfolioId.pathParamSerializer);
        // @formatter:on

    }
}
