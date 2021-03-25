package com.redelastic.stocktrader.broker.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

@JsonDeserialize(builder = DetailedQuote.DetailedQuoteBuilder.class)
@Builder
@Data
public class DetailedQuote {
    String symbol;
    Company company;
    Quote quote;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class DetailedQuoteBuilder {
    }
}
