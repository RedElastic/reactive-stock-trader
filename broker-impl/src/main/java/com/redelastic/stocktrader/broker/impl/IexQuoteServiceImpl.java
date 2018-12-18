package com.redelastic.stocktrader.broker.impl;

import com.redelastic.stocktrader.broker.api.Quote;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

/**
 * Delegate quotes out to the IexTrading public API.
 */
public class IexQuoteServiceImpl implements QuoteService {
    private WSClient wsClient;

    @Inject
    IexQuoteServiceImpl(WSClient wsClient) {
        this.wsClient = wsClient;
    }

    public CompletionStage<Quote> getQuote(String symbol) {
        // TODO move this out into a configuration file
        String url = String.format("https://api.iextrading.com/1.0/stock/%s/quote", symbol);
        CompletionStage<WSResponse> request = wsClient.url(url).get();
        return request.thenApply(response ->
            Quote.builder()
                    .symbol(symbol)
                    .sharePrice(new BigDecimal(("10.40")))
                    .build()
        );
    }
}
