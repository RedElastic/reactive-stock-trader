package com.redelastic.stocktrader.broker.impl.quote;

import com.redelastic.stocktrader.broker.api.DetailedQuotesResponse;
import com.redelastic.stocktrader.broker.api.Quote;

import java.util.concurrent.CompletionStage;

public interface QuoteService {
    CompletionStage<Quote> getQuote(String symbol);
    CompletionStage<DetailedQuotesResponse> getDetailedQuotes(String req);
}
