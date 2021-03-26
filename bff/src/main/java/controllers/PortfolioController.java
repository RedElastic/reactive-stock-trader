package controllers;

import com.redelastic.CSHelper;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.DetailedQuote;
import com.redelastic.stocktrader.broker.api.DetailedQuotesResponse;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.broker.api.OrderSummary;
import com.redelastic.stocktrader.portfolio.api.Holding;
import com.redelastic.stocktrader.portfolio.api.OpenPortfolioDetails;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import com.redelastic.stocktrader.portfolio.api.PortfolioView;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.order.OrderType;
import controllers.forms.portfolio.OpenPortfolioForm;
import controllers.forms.portfolio.PlaceOrderForm;
import lombok.val;
import models.CompletedOrder;
import models.EquityHolding;
import models.PortfolioSummary;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class PortfolioController extends Controller {

    private final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;
    private final BrokerService brokerService;

    private final Form<PlaceOrderForm> placeOrderForm;
    private final Form<OpenPortfolioForm> openPortfolioForm;

    @Inject
    private PortfolioController(PortfolioService portfolioService,
                                BrokerService brokerService,
                                FormFactory formFactory) {
        this.portfolioService = portfolioService;
        this.brokerService = brokerService;
        this.placeOrderForm = formFactory.form(PlaceOrderForm.class);
        this.openPortfolioForm = formFactory.form(OpenPortfolioForm.class);
    }

    public CompletionStage<Result> getPortfolio(String portfolioId) {

        CompletableFuture<PortfolioView> portfolioView = portfolioService
                .getPortfolio(new PortfolioId(portfolioId))
                .invoke()
                .toCompletableFuture();

        CompletableFuture<DetailedQuotesResponse> detailedQuotes = portfolioView
                .thenApply(PortfolioView::getHoldings)
                .thenApply(this::holdingsToStrings)
                .thenCompose(r -> {
                        if (r.isEmpty()) {
                                return CompletableFuture.completedFuture(null);
                        }
                        else {
                                return brokerService.getDetailedQuotes(r).invoke();
                        }
                }).toCompletableFuture();
        
        CompletableFuture<PSequence<CompletedOrder>> completedOrders =
                        portfolioView
                                .thenApply(PortfolioView::getCompletedOrders)
                                .thenCompose(this::completedOrders)
                                .toCompletableFuture();
        
        CompletionStage<PortfolioSummary> summary = CompletableFuture
                .allOf(portfolioView, detailedQuotes)
                .thenApply(done -> {
                    PortfolioView pv = portfolioView.join();
                    DetailedQuotesResponse dqr = detailedQuotes.join();
                    PSequence<CompletedOrder> co = completedOrders.join();

                    Map<String, DetailedQuote> quoteMap = new HashMap<String, DetailedQuote>();
                    if (dqr != null) {
                        for (DetailedQuote quote: dqr.getDetailedQuotes()) {
                                quoteMap.put(quote.getSymbol(), quote);
                        }
                    }

                    List<EquityHolding> equities = new ArrayList<EquityHolding>();
                    if (dqr != null) {
                        for (Holding holding : pv.getHoldings()) {
                            DetailedQuote quote = quoteMap.get(holding.getSymbol());
                            EquityHolding pricedHolding = 
                                EquityHolding.builder()
                                        .symbol(quote.getSymbol())
                                        .shares(holding.getShareCount())
                                        .currentValue(quote.getQuote().getLatestPrice().multiply(new BigDecimal(holding.getShareCount())))
                                        .detailedQuote(quote)
                                        .build();
                               equities.add(pricedHolding);                                 
                        }
                    }
                    
                return PortfolioSummary.builder()
                        .portfolioId(pv.getPortfolioId().getId())
                        .name(pv.getName())
                        .funds(pv.getFunds())
                        .equities(ConsPStack.from(equities))
                        .completedOrders(co)
                        .build();
        });

        return summary
                .thenApply(Json::toJson)
                .thenApply(Results::ok);
    }

    private String holdingsToStrings(PSequence<Holding> holdings) {
            String s = holdings.stream().map(Holding::getSymbol).collect(Collectors.joining(","));
            System.out.println("holdingsToStrings: " + s);
            return s;
    }

    public CompletionStage<Result> getAllPortfolios() {
        val portfolios = portfolioService
                .getAllPortfolios()
                .invoke();

        return portfolios
                .thenApply(Json::toJson)
                .thenApply(Results::ok);
    }

    private CompletionStage<PSequence<CompletedOrder>> completedOrders(PSequence<OrderId> orderIds) {
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

    private CompletedOrder toCompletedOrder(OrderId orderId, @Nullable OrderSummary orderSummary) {
        if (orderSummary != null) {
            val builder = CompletedOrder.builder()
                    .orderId(orderId.getId())
                    .symbol(orderSummary.getSymbol())
                    .shares(orderSummary.getShares())
                    .tradeType(orderSummary.getTradeType());
            if (orderSummary.getStatus() instanceof OrderStatus.Fulfilled) {
                builder.price(((OrderStatus.Fulfilled) orderSummary.getStatus()).getPrice());
            }
            return builder.build();
        } else {
            return CompletedOrder.builder()
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
                        val jsonResult = Json.newObject()
                                .put("portfolioId", portfolioId.getId());
                        return Results.created(jsonResult);
                    });
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
                    .thenApply(orderId -> {
                        val jsonResult = Json.newObject()
                                .put("orderId", orderId.getId());
                        return Results.status(Http.Status.ACCEPTED, jsonResult);
                    });

        }
    }

}
