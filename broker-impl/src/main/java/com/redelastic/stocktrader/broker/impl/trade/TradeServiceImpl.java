package com.redelastic.stocktrader.broker.impl.trade;

import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.broker.impl.quote.QuoteService;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public class TradeServiceImpl implements TradeService {

    private final Logger log = LoggerFactory.getLogger(TradeServiceImpl.class);

    private final QuoteService quoteService;

    @Inject
    public TradeServiceImpl(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public CompletionStage<OrderResult> placeOrder(Order order) {
        if (order.getDetails().getConditions() instanceof OrderConditions.Market) {
            return completeMarketOrder(order);
        } else {
            log.error(String.format("Unhandled order placed: %s",
                    order.getDetails().getConditions()));
            throw new UnsupportedOperationException();
        }
    }

    private CompletionStage<OrderResult> completeMarketOrder(Order order) {
        return priceOrder(order).thenApply(price -> {
            OrderDetails details = order.getDetails();
            Trade trade = Trade.builder()
                    .orderType(details.getOrderType())
                    .symbol(details.getSymbol())
                    .shares(details.getShares())
                    .price(price)
                    .build();
            return OrderResult.OrderCompleted.builder()
                    .orderId(order.getOrderId())
                    .portfolioId(details.getPortfolioId())
                    .trade(trade)
                    .build();
        });
    }

    private CompletionStage<BigDecimal> priceOrder(Order order) {
        return quoteService
                .getQuote(order.getDetails().getSymbol())
                .thenApply(quote ->
                    quote.getSharePrice().multiply(BigDecimal.valueOf(order.getDetails().getShares())));
    }
}
