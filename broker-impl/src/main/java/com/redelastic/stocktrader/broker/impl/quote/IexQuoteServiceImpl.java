package com.redelastic.stocktrader.broker.impl.quote;

import com.fasterxml.jackson.databind.JsonNode;
import com.redelastic.stocktrader.broker.api.Quote;
import com.redelastic.stocktrader.broker.impl.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Delegate quotes out to the IexTrading public API.
 */
public class IexQuoteServiceImpl implements QuoteService, WSBodyReadables {

    private final Logger log = LoggerFactory.getLogger(IexQuoteServiceImpl.class);

    private final WSClient wsClient;

    @Inject
    IexQuoteServiceImpl(WSClient wsClient) {
        this.wsClient = wsClient;
    }

    public CompletionStage<Quote> getQuote(String symbol) {
        // TODO move this out into a configuration file
        String url = String.format("https://api.iextrading.com/1.0/stock/%s/quote", symbol);
        CompletionStage<WSResponse> request = wsClient.url(url).get();
        return request
                .thenApply(response -> response.getBody(json()))
                .thenApply(json -> Json.fromJson(json, IexQuoteResponse.class))
                .thenApply(iexResponse ->
                    Quote.builder()
                            .symbol(symbol)
                            .sharePrice(iexResponse.getLatestPrice())
                            .build());
    }
}
