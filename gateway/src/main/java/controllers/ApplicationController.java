package controllers;

import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class ApplicationController extends Controller {

    private PortfolioService portfolioService;

    @Inject
    public ApplicationController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    public Result index() {
        return null;
    }
}