package com.redelastic.stocktrader.portfolio.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = NewPortfolioRequest.NewPortfolioRequestBuilder.class)
public class NewPortfolioRequest {
    //String linkedAccount;

    String name;
}
