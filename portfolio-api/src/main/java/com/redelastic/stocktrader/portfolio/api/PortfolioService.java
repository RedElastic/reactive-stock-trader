package com.redelastic.stocktrader.portfolio.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderId;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 *
 */
public interface PortfolioService extends Service {

    ServiceCall<OpenPortfolioDetails, PortfolioId> openPortfolio();

    /**
     * Place an order for a particular portfolio.
     * @param portfolioId ID for the portfolio placing the order.
     * @return Order ID when the order has been accepted. For a sell order this requires confirming that the
     * requested number of shares are available to be sold.
     */
    ServiceCall<OrderDetails, OrderId> placeOrder(PortfolioId portfolioId);

    /**
     * Sell all equities (as market sell), then transfer all funds out, then close the portfolio.
     * Note that after selling all the equities the portfolio may still be in an overdrawn state (negative funds),
     * we will not close it until the balance of funds is zero. External action will be required to complete closure
     * of an overdrawn account.
     * @param portfolioId ID for the portfolio to liquidate.
     * @return Done when the liquidate command has been acknowledged.
     */
    ServiceCall<NotUsed, Done> liquidatePortfolio(PortfolioId portfolioId);

    /**
     * Get a view of the portfolio, including the current valuation of the equities held in it.
     * @param portfolioId ID of the portfolio to view.
     * @return The current portfolio's state.
     */
    ServiceCall<NotUsed, PortfolioView> getPortfolio(PortfolioId portfolioId);

    ServiceCall<FundsTransfer, Done> processTransfer(PortfolioId portfolioId);

    /**
     * The orders placed by portfolios managed by this service.
     * @return Orders placed by portfolios.
     */
    Topic<OrderPlaced> orderPlaced();

    Topic<FundsTransfer> fundsTransfer();

    @Override
    default Descriptor descriptor() {
        String ORDERS_TOPIC_ID = "Portfolio-OrderPlaced";

        // @formatter:off
        return named("portfolio").withCalls(
                // Use restCall to make it explicit that this is an ordinary HTTP endpoint
                restCall(Method.POST, "/api/portfolio", this::openPortfolio),
                restCall(Method.POST,"/api/portfolio/:portfolioId/liquidate", this::liquidatePortfolio),
                restCall(Method.GET,"/api/portfolio/:portfolioId", this::getPortfolio),
                restCall(Method.POST,"/api/portfolio/:portfolioId/placeOrder", this::placeOrder)
        ).withTopics(
            topic(ORDERS_TOPIC_ID, this::orderPlaced)
        ).withPathParamSerializer(PortfolioId.class, new PathParamSerializer<PortfolioId>() {
            @Override
            public PSequence<String> serialize(PortfolioId parameter) {
                return ConsPStack.singleton(parameter.getId());
            }

            @Override
            public PortfolioId deserialize(PSequence<String> parameters) {
                return new PortfolioId(parameters.get(0)); // FIXME: how do we handle errors?
            }
        });
        // @formatter:on

    }
}
