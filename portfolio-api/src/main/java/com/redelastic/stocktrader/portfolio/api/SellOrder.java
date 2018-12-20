package com.redelastic.stocktrader.portfolio.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;

@Value
@JsonDeserialize
public final class SellOrder {

    String portfolioId;

    String stockSymbol;

    int shares;
}
