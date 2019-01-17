package controllers;

import com.redelastic.stocktrader.order.OrderConditions;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class PortfolioController extends Controller {

    private final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;

    private final Form<PlaceOrderForm> placeOrderForm;
    private final Form<OpenPortfolioForm> openPortfolioForm;

    @Inject
    private PortfolioController(PortfolioService portfolioService,
                                FormFactory formFactory) {
        this.portfolioService = portfolioService;
        this.placeOrderForm = formFactory.form(PlaceOrderForm.class);
        this.openPortfolioForm = formFactory.form(OpenPortfolioForm.class);
    }

    public CompletionStage<Result> getPortfolio(String portfolioId) {
        return portfolioService
            .getPortfolio(portfolioId)
            .invoke()
            .thenApply(Json::toJson)
            .thenApply(Results::ok);
    }

    public CompletionStage<Result> openPortfolio() {
        OpenPortfolioDetails openRequest = openPortfolioForm.bindFromRequest().get().toRequest(); // TODO handle errors
        return portfolioService
                .openPortfolio()
                .invoke(openRequest)
                .thenApply(Results::ok);
    }

    public CompletionStage<Result> placeOrder(String portfolioId) {
        PlaceOrderForm orderForm = placeOrderForm.bindFromRequest().get(); // TODO handle errors

        OrderDetails order = OrderDetails.builder()
                .orderType(orderForm.getOrderType())
                .symbol(orderForm.getSymbol())
                .shares(orderForm.getShares())
                .orderConditions(OrderConditions.Market.INSTANCE)
                .build();
        return portfolioService
                .placeOrder(portfolioId)
                .invoke(order)
                .thenApply(done -> ok());
    }

}
