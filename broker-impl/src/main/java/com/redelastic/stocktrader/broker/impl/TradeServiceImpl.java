package com.redelastic.stocktrader.broker.impl;

import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.Quote;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public class TradeServiceImpl implements TradeService {

    private Logger log = LoggerFactory.getLogger(TradeServiceImpl.class);

    private QuoteService quoteService;

    @Inject
    public TradeServiceImpl(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public CompletionStage<OrderResult> placeOrder(Order order) {
        if (order.getConditions() instanceof OrderConditions.Market) {
            return completeMarketOrder(order);
        } else {
            log.error(String.format("Unhandled order placed: %s", order.getConditions()));
            throw new UnsupportedOperationException();
        }
    }

    private CompletionStage<OrderResult> completeMarketOrder(Order order) {
        return priceOrder(order).thenApply(price -> {
            Trade trade = Trade.builder()
                    .orderType(order.getOrderType())
                    .symbol(order.getSymbol())
                    .shares(order.getShares())
                    .price(price)
                    .build();
            return OrderResult.OrderCompleted.builder()
                    .orderId(order.getOrderId())
                    .portfolioId(order.getPortfolioId())
                    .trade(trade)
                    .build();
        });
    }

    private CompletionStage<BigDecimal> priceOrder(Order order) {
        CompletionStage<Quote> getQuote = quoteService.getQuote(order.getSymbol());
        return  getQuote.thenApply(quote ->
            quote.getSharePrice().multiply(BigDecimal.valueOf(order.getShares())));
    }
}
