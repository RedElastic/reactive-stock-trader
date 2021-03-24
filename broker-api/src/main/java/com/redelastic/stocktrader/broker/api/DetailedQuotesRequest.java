package com.redelastic.stocktrader.broker.api;

import lombok.NonNull;
import lombok.Value;
import lombok.Builder;

import org.pcollections.PSequence;

@Value
@Builder
public class DetailedQuotesRequest {
    @NonNull PSequence<String> symbols;
}