package com.redelastic.stocktrader.portfolio.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;

@Value
@JsonDeserialize
public final class SellOrder {

    @NonNull
    public final String portfolioId;

    @NonNull
    public final String stockSymbol;

    @Nonnegative
    public final int shares;
}
