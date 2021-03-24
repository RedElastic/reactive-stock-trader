package com.redelastic.stocktrader.broker.impl.quote;

import akka.actor.ActorSystem;
import akka.pattern.CircuitBreaker;
import com.fasterxml.jackson.databind.JsonNode;
import com.redelastic.stocktrader.broker.api.DetailedQuotesRequest;
import com.redelastic.stocktrader.broker.api.DetailedQuotesResponse;
import com.redelastic.stocktrader.broker.api.Quote;
import com.typesafe.config.Config;

import org.pcollections.PSequence;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * Delegate quotes out to the IexTrading public API.
 */
public class IexQuoteServiceImpl implements QuoteService, WSBodyReadables {

    private final Logger log = LoggerFactory.getLogger(IexQuoteServiceImpl.class);

    private final WSClient wsClient;
    private final String hostName;
    private final String token;
    private final Duration requestTimeout;

    private final CircuitBreaker circuitBreaker;

    @Inject
    IexQuoteServiceImpl(WSClient wsClient,
                        Config config,
                        ActorSystem actorSystem) {
        this.wsClient = wsClient;
        this.hostName = config.getString("quote.iex.hostname");
        this.token = config.getString("quote.iex.token");
        this.requestTimeout = Duration.ofMillis(1000); 
        int maxFailures = 10;
        Duration callTimeout = this.requestTimeout.minus(this.requestTimeout.dividedBy(10));
        Duration resetTimeout = Duration.ofMillis(1000);
        this.circuitBreaker = new CircuitBreaker(
                actorSystem.getDispatcher(),
                actorSystem.getScheduler(),
                maxFailures,
                callTimeout,
                resetTimeout); // TODO
    }

    public CompletionStage<Quote> getQuote(String symbol) {
        CompletionStage<WSResponse> request =
                circuitBreaker.callWithCircuitBreakerCS(() ->
                        quoteRequest(symbol)
                                .setRequestTimeout(requestTimeout)
                                .get());

        request.thenAccept(response -> {
        	log.info(response.toString());
        });

        return request
                .thenApply(response -> {
                    JsonNode json = response.getBody(json());
                    IexQuoteResponse iexQuoteResponse = Json.fromJson(json, IexQuoteResponse.class);
                    return Quote.builder()
                            .symbol(symbol)
                            .sharePrice(iexQuoteResponse.getLatestPrice())
                            .build();
                });
    }

    public CompletionStage<DetailedQuotesResponse> getDetailedQuotes(String req) {
        CompletionStage<WSResponse> request =
                circuitBreaker.callWithCircuitBreakerCS(() ->
                        detailedQuotesRequest(req)
                                .setRequestTimeout(requestTimeout)
                                .get());

        request.thenAccept(response -> {
        	log.info(response.toString());
        });

        return request
                .thenApply(response -> {
                    JsonNode json = response.getBody(json());
                    DetailedQuotesResponse detailedQuoteResponse = Json.fromJson(json, DetailedQuotesResponse.class);
                    return detailedQuoteResponse;
                });
    }

    private WSRequest quoteRequest(String symbol) {
        String url = String.format("%s/stock/%s/quote/?token=%s", this.hostName, symbol, this.token);
        log.info("quote url: " + url);
        return wsClient.url(url);
    }

    private WSRequest detailedQuotesRequest(String symbols) {
        // /stock/market/batch?symbols=aapl,fb,tsla&types=quote,news,chart&range=1m&last=5
        String url = String.format("%s/stock/market/batch/?symbols=%s&types=quotes,company?token=%s", this.hostName, symbols, this.token);
        log.info("quote url: " + url);
        return wsClient.url(url);
    }

}
