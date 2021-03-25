package com.redelastic.stocktrader.broker.impl.quote;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

@JsonDeserialize(builder = IexDetailedQuoteResponse.IexDetailedQuoteResponseBuilder.class)
@Builder
@Data
class IexDetailedQuoteResponse {
    String symbol;
    IexCompany company;
    IexQuote quote;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class IexDetailedQuoteBuilder {
    }
}
