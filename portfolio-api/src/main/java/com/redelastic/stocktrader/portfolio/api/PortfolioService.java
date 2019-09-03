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

import org.pcollections.PSequence;

import static com.lightbend.lagom.javadsl.api.Service.*;

import akka.stream.javadsl.Source;

public interface PortfolioService extends Service {

    String ORDERS_TOPIC_ID = "Portfolio-OrderPlaced";

    ServiceCall<OpenPortfolioDetails, PortfolioId> openPortfolio();

    ServiceCall<OrderDetails, OrderId> placeOrder(PortfolioId portfolioId);

    ServiceCall<NotUsed, Done> closePortfolio(PortfolioId portfolioId);

    ServiceCall<FundsTransfer, Done> processTransfer(PortfolioId portfolioId);

    ServiceCall<NotUsed, PortfolioView> getPortfolio(PortfolioId portfolioId);

    ServiceCall<NotUsed, PSequence<PortfolioSummary>> getAllPortfolios();

    Topic<OrderPlaced> orderPlaced();

    @Override
    default Descriptor descriptor() {

        // @formatter:off
        return named("reactivestock-portfolio").withCalls(
                // Use restCall to make it explicit that this is an ordinary HTTP endpoint
                restCall(Method.POST, "/api/portfolio", this::openPortfolio),
                restCall(Method.POST, "/api/portfolio/:portfolioId/close", this::closePortfolio),
                restCall(Method.GET, "/api/portfolio", this::getAllPortfolios),
                restCall(Method.GET, "/api/portfolio/:portfolioId", this::getPortfolio),
                restCall(Method.POST, "/api/portfolio/:portfolioId/placeOrder", this::placeOrder),
                restCall(Method.POST, "/api/portfolio/:portfolio/processTransfer", this::processTransfer)
        ).withTopics(
                topic(ORDERS_TOPIC_ID, this::orderPlaced)
        ).withPathParamSerializer(PortfolioId.class, PortfolioId.pathParamSerializer);
        // @formatter:on

    }
}
