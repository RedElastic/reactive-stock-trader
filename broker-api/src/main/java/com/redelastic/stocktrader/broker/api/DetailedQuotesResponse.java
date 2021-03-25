package com.redelastic.stocktrader.broker.api;

import lombok.Builder;
import lombok.Value;

import org.pcollections.PSequence;

@Value
@Builder
public class DetailedQuotesResponse {

    public PSequence<DetailedQuote> detailedQuotes;

}