/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.broker.impl.quote;

import com.redelastic.stocktrader.broker.api.Quote;

import java.util.concurrent.CompletionStage;

public interface QuoteService {
    CompletionStage<Quote> getQuote(String symbol);
}
