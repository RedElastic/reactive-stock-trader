package com.redelastic.stocktrader.broker.impl.quote;

import com.redelastic.stocktrader.broker.api.Quote;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Delegate quotes out to the IexTrading public API.
 */
public class IexQuoteServiceImpl implements QuoteService, WSBodyReadables {

    private final Logger log = LoggerFactory.getLogger(IexQuoteServiceImpl.class);

    private final WSClient wsClient;
    private final String hostName;

    @Inject
    IexQuoteServiceImpl(WSClient wsClient,
                        Config config) {
        this.wsClient = wsClient;
        this.hostName = config.getString("quote.iex.hostname");
    }

    private WSRequest quoteRequest(String symbol) {
        String url = String.format("https://%s/1.0/stock/%s/quote", this.hostName, symbol);
        return wsClient.url(url);
    }

    public CompletionStage<Quote> getQuote(String symbol) {
        CompletionStage<WSResponse> request = quoteRequest(symbol).get();
        return request
                .thenApply(response -> {
                    log.info(response.getBody());
                    return response.getBody(json());
                })
                .thenApply(json -> Json.fromJson(json, IexQuoteResponse.class))
                .thenApply(iexResponse ->
                        Quote.builder()
                                .symbol(symbol)
                                .sharePrice(iexResponse.getLatestPrice())
                                .build());
    }
}
