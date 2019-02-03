package controllers;

import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.order.OrderType;
import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import controllers.forms.portfolio.OpenPortfolioForm;
import controllers.forms.portfolio.PlaceOrderForm;
import lombok.val;
import models.PortfolioView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.quote.QuoteService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@SuppressWarnings("WeakerAccess")
public class PortfolioController extends Controller {

    private final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;
    private final QuoteService quoteService;

    private final Form<PlaceOrderForm> placeOrderForm;
    private final Form<OpenPortfolioForm> openPortfolioForm;

    @Inject
    private PortfolioController(PortfolioService portfolioService,
                                QuoteService quoteService,
                                FormFactory formFactory) {
        this.portfolioService = portfolioService;
        this.quoteService = quoteService;
        this.placeOrderForm = formFactory.form(PlaceOrderForm.class);
        this.openPortfolioForm = formFactory.form(OpenPortfolioForm.class);
    }

    public CompletionStage<Result> getPortfolio(String portfolioId) {
        val portfolioView = portfolioService
                .getPortfolio(new PortfolioId(portfolioId))
                .invoke();

        val pricedView = portfolioView
                .thenCompose(view ->
                        quoteService.priceHoldings(view.getHoldings())
                            .thenApply(pricedHoldings ->
                                    PortfolioView.builder()
                                        .portfolioId(view.getPortfolioId().getId())
                                        .name(view.getName())
                                        .funds(view.getFunds())
                                        .holdings(pricedHoldings)
                                        .build()
                                    )
                        );

        return pricedView
            .thenApply(Json::toJson)
            .thenApply(Results::ok);
    }

    public CompletionStage<Result> openPortfolio() {
        Form<OpenPortfolioForm> form = openPortfolioForm.bindFromRequest();
        if (form.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(form.errorsAsJson()));
        } else {
            OpenPortfolioDetails openRequest = form.get().toRequest();
            return portfolioService
                    .openPortfolio()
                    .invoke(openRequest)
                    .thenApply(PortfolioId::getId)
                    .thenApply(Results::created);
        }
    }

    public CompletionStage<Result> placeOrder(String portfolioId) {
        Form<PlaceOrderForm> form = placeOrderForm.bindFromRequest();
        if (form.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(form.errorsAsJson()));
        } else {
            PlaceOrderForm orderForm = form.get();

            OrderDetails order = OrderDetails.builder()
                    .tradeType(orderForm.getOrder().toTradeType())
                    .symbol(orderForm.getSymbol())
                    .shares(orderForm.getShares())
                    .orderType(OrderType.Market.INSTANCE)
                    .build();
            return portfolioService
                    .placeOrder(new PortfolioId(portfolioId))
                    .invoke(order)
                    .thenApply(done -> created());
        }
    }

}
