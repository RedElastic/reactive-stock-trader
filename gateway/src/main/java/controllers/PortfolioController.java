package controllers;

import com.redelastic.stocktrader.portfolio.api.NewPortfolioRequest;
import com.redelastic.stocktrader.portfolio.api.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import play.libs.Json;
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

    public CompletionStage<Result> openPortfolio(NewPortfolioRequest request) {
        return portfolioService
                .openPortfolio()
                .invoke(request)
                .thenApply(r -> ok());
    }

}
