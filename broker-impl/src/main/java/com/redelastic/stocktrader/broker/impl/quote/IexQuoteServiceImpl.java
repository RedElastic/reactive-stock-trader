package com.redelastic.stocktrader.broker.impl.quote;

import akka.actor.ActorSystem;
import akka.pattern.CircuitBreaker;
import com.fasterxml.jackson.databind.JsonNode;
import com.redelastic.stocktrader.broker.api.Company;
import com.redelastic.stocktrader.broker.api.DetailedQuote;
import com.redelastic.stocktrader.broker.api.DetailedQuotesResponse;
import com.typesafe.config.Config;

import org.pcollections.ConsPStack;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.CompletionStage;

import com.redelastic.stocktrader.broker.api.Quote;

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
                resetTimeout); 
    }

    public CompletionStage<Quote> getQuote(String symbol) {
        CompletionStage<WSResponse> request =
                circuitBreaker.callWithCircuitBreakerCS(() ->
                        quoteRequest(symbol)
                                .setRequestTimeout(requestTimeout)
                                .get());

        request.thenAccept(response -> {
        	log.info(response.asJson().toString());
        });

        return request
                .thenApply(response -> {
                    JsonNode json = response.getBody(json());
                    return Json.fromJson(json, Quote.class);
                });
    }

    public CompletionStage<DetailedQuotesResponse> getDetailedQuotes(String req) {
        CompletionStage<WSResponse> request =
                circuitBreaker.callWithCircuitBreakerCS(() ->
                        detailedQuotesRequest(req)
                                .setRequestTimeout(requestTimeout)
                                .get());
                                
        request.thenAccept(response -> {
            log.info(response.asJson().toString());
        });

        return request
            .thenApply(response -> {
                JsonNode json = response.getBody(json());
                TreePVector<DetailedQuote> quotes = TreePVector.empty();
                for (Iterator<JsonNode> jsonIterator = json.iterator(); jsonIterator.hasNext(); ) {
                    JsonNode node = jsonIterator.next();
                    Company company = Json.fromJson(node.get("company"), Company.class);
                    Quote quote = Json.fromJson(node.get("quote"), Quote.class);
                    String stock = company.getSymbol();
                    DetailedQuote dqr = DetailedQuote.builder()
                                                .symbol(stock)
                                                .company(company)
                                                .quote(quote)
                                                .build();
                    quotes = quotes.plus(dqr);
                }
                return DetailedQuotesResponse.builder().detailedQuotes(quotes).build();
            });
    }

    private WSRequest quoteRequest(String symbol) {
        String url = String.format("%s/stock/%s/quote/?token=%s", this.hostName, symbol, this.token);
        log.info("quote url: " + url);
        return wsClient.url(url);
    }

    private WSRequest detailedQuotesRequest(String symbols) {
        String url = String.format("%s/stock/market/batch?symbols=%s&types=quote,company&range=5y&token=%s", this.hostName, symbols, this.token);
        log.info("quote url: " + url);
        return wsClient.url(url);
    }

}
