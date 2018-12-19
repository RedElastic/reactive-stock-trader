package com.redelastic.stocktrader.portfolio.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.concurrent.Immutable;

@Value
@Immutable
@JsonDeserialize
public final class NewPortfolioRequest {
    @NonNull
    PortfolioId portfolioId;

    @NonNull
    String linkedAccount;
}
