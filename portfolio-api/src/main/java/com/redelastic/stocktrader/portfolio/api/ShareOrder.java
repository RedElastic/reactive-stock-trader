package com.redelastic.stocktrader.portfolio.api;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;
import lombok.NonNull;

@Value
@JsonDeserialize
public class ShareOrder {

    String portfolioId;

    String stockSymbol;

    int shares;
}