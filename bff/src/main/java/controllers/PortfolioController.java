package controllers;

import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderType;
import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import controllers.forms.portfolio.OpenPortfolioForm;
import controllers.forms.portfolio.PlaceOrderForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@SuppressWarnings("WeakerAccess")
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
            .getPortfolio(new PortfolioId(portfolioId))
            .invoke()
            .thenApply(Json::toJson)
            .thenApply(Results::ok);
    }

    public CompletionStage<Result> openPortfolio() {
        OpenPortfolioDetails openRequest = openPortfolioForm.bindFromRequest().get().toRequest(); // TODO handle errors
        return portfolioService
                .openPortfolio()
                .invoke(openRequest)
                .thenApply(PortfolioId::getId)
                .thenApply(Results::ok);
    }

    public CompletionStage<Result> placeOrder(String portfolioId) {
        PlaceOrderForm orderForm = placeOrderForm.bindFromRequest().get(); // TODO handle errors

        OrderDetails order = OrderDetails.builder()
                .tradeType(orderForm.getOrder().toTradeType())
                .symbol(orderForm.getSymbol())
                .shares(orderForm.getShares())
                .orderType(OrderType.Market.INSTANCE)
                .build();
        return portfolioService
                .placeOrder(new PortfolioId(portfolioId))
                .invoke(order)
                .thenApply(done -> ok());
    }

}
