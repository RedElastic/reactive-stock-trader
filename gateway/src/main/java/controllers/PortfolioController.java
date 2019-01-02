package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class PortfolioController extends Controller {

    private final PortfolioService portfolioService;

    @Inject
    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    public CompletionStage<Result> getPortfolio(String portfolioId) {
        return portfolioService
            .getPortfolio()
            .invoke(new PortfolioId(portfolioId))
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
                .thenApply(portfolioId -> ok(portfolioId.getId()));
    }

}
