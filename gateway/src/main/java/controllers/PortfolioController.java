package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.redelastic.stocktrader.order.OrderConditions;
import com.redelastic.stocktrader.order.OrderType;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.portfolio.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class PortfolioController extends Controller {

    private Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;

    @Inject
    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    public CompletionStage<Result> getPortfolio(String portfolioId) {
        return portfolioService
            .getPortfolio(portfolioId)
            .invoke()
            .thenApply(Json::toJson)
            .thenApply(Results::ok);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> openPortfolio() {
        JsonNode json = request().body().asJson();
        NewPortfolioRequest openRequest = Json.fromJson(json, NewPortfolioRequest.class);
        return portfolioService
                .openPortfolio()
                .invoke(openRequest)
                .thenApply(portfolioId -> ok(portfolioId));
    }

    public CompletionStage<Result> placeOrder(String portfolioId) {
        JsonNode json = request().body().asJson();
        // TODO: Parse and generate order
        //Order order = Json.fromJson(json, Order.class);
        Order order = Order.builder()
                .orderType(OrderType.BUY)
                .symbol("IBM")
                .shares(10)
                .conditions(OrderConditions.Market.INSTANCE)
                .portfolioId(portfolioId)
                .build();
        log.warn(order.toString());
        return portfolioService
                .placeOrder(portfolioId)
                .invoke(order)
                .thenApply(done -> ok());
    }

}
