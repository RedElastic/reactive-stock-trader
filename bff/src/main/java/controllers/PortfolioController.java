package controllers;

import com.lightbend.lagom.javadsl.api.transport.PolicyViolation;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import com.redelastic.CSHelper;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.broker.api.OrderSummary;
import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import controllers.forms.portfolio.OpenPortfolioForm;
import controllers.forms.portfolio.PlaceOrderForm;
import lombok.val;
import models.Order;
import models.EquityHolding;
import models.Portfolio;
import models.PortfolioSummary;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.quote.QuoteService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("WeakerAccess")
public class PortfolioController extends Controller {

    private final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;
    private final QuoteService quoteService;
    private final BrokerService brokerService;

    private final Form<PlaceOrderForm> placeOrderForm;
    private final Form<OpenPortfolioForm> openPortfolioForm;

    @Inject
    private PortfolioController(PortfolioService portfolioService,
                                QuoteService quoteService,
                                BrokerService brokerService,
                                FormFactory formFactory) {
        this.portfolioService = portfolioService;
        this.quoteService = quoteService;
        this.brokerService = brokerService;
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
                                        Portfolio.builder()
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

    public CompletionStage<Result> getSummary(String portfolioId, Boolean includeOrderInfo, Boolean includePrices) {
        val getModel = portfolioService
                .getPortfolio(new PortfolioId(portfolioId))
                .invoke()
                .toCompletableFuture();

        CompletableFuture<PSequence<Order>> getCompletedOrders =
                includeOrderInfo ?
                        getModel
                                .thenApply(PortfolioView::getCompletedOrders)
                                .thenCompose(this::getOrderDetails)
                                .toCompletableFuture()
                        : CompletableFuture.completedFuture(null);

        CompletableFuture<PSequence<Order>> getPendingOrders =
                includeOrderInfo ?
                        getModel
                                .thenApply(PortfolioView::getPendingOrders)
                                .thenCompose(this::getOrderDetails)
                                .toCompletableFuture()
                        : CompletableFuture.completedFuture(null);

        CompletableFuture<PSequence<EquityHolding>> getEquityHoldings =
                includePrices ?
                        getModel
                                .<PSequence<EquityHolding>>thenApply(model ->
                                        ConsPStack.from(
                                                model.getHoldings().stream().map(holding ->
                                                        EquityHolding.builder()
                                                                .symbol(holding.getSymbol())
                                                                .shares(holding.getShareCount())
                                                                .build()
                                                ).collect(toList())))
                                .toCompletableFuture()
                        : CompletableFuture.completedFuture(null);

        val summaryView = CompletableFuture
                .allOf(getModel, getCompletedOrders, getPendingOrders, getEquityHoldings)
                .thenApply(done -> {
                    PortfolioView model = getModel.join();
                    PSequence<Order> completedOrders = getCompletedOrders.join();
                    PSequence<Order> pendingOrders = getPendingOrders.join();
                    PSequence<EquityHolding> equities = getEquityHoldings.join();

                    return PortfolioSummary.builder()
                            .portfolioId(model.getPortfolioId().getId())
                            .name(model.getName())
                            .funds(model.getFunds())
                            .completedOrders(completedOrders)
                            .pendingOrders(pendingOrders)
                            .equities(equities)
                            .build();
                });

        return summaryView
                .thenApply(Json::toJson)
                .thenApply(Results::ok);
    }

    private CompletionStage<PSequence<Order>> getOrderDetails(PSequence<OrderId> orderIds) {
        return CSHelper.allOf(
                orderIds.stream()
                        .map(orderId ->
                                brokerService
                                        .getOrderSummary(orderId)
                                        .invoke()
                                        .thenApply(o -> {
                                            log.info(o.toString());
                                            return o;
                                        })
                                        .exceptionally(ex -> {
                                            ex.printStackTrace();
                                            return Optional.empty();
                                        })
                                        .thenApply(summary -> toCompletedOrder(orderId, summary.orElse(null)))
                                        .toCompletableFuture())
                        .collect(toList())
        ).thenApply(ConsPStack::from);
    }

    private Order toCompletedOrder(OrderId orderId, @Nullable OrderSummary orderSummary) {
        if (orderSummary != null) {
            val builder = Order.builder()
                    .orderId(orderId.getId())
                    .symbol(orderSummary.getSymbol())
                    .shares(orderSummary.getShares())
                    .tradeType(orderSummary.getTradeType());
            if (orderSummary.getStatus() instanceof OrderStatus.Fulfilled) {
                builder.price(((OrderStatus.Fulfilled) orderSummary.getStatus()).getPrice());
            }
            return builder.build();
        } else {
            return Order.builder()
                    .orderId(orderId.getId())
                    .build();
        }
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
                    .thenApply(portfolioId -> {
                        val jsonResult = Json.newObject();
                        jsonResult.put("portfolioId", portfolioId.getId());
                        return jsonResult;
                    })
                    .thenApply(Results::created);
        }
    }

    public CompletionStage<Result> placeOrder(String portfolioId) {
        Form<PlaceOrderForm> form = placeOrderForm.bindFromRequest();
        if (form.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(form.errorsAsJson()));
        } else {
            PlaceOrderForm orderForm = form.get();

            CompletionStage<Result> response = portfolioService
                    .placeOrder(new PortfolioId(portfolioId))
                    .invoke(orderForm.toOrderDetails())
                    .thenApply(orderId ->
                            Results.created(
                                Json.newObject().put("orderId", orderId.getId())));

            return CSHelper.recover(response, TransportException.class,
                    ex -> {
                log.error("cshelper recover", ex);
                log.error(String.format("error code: %s", ex.errorCode().toString()));
                return Results.status(ex.errorCode().http(), Json.newObject().put("error", ex.getMessage()));
                    });
        }
    }

}
