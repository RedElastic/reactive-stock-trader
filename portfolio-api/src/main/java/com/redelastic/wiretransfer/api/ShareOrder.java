package com.redelastic.wiretransfer.api;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;
import lombok.NonNull;

@Immutable
@Value
@JsonDeserialize
public final class ShareOrder {

    @NonNull
    public final String portfolioId;

    @NonNull
    public final String stockSymbol;

    @Nonnegative
    public final int shares;
}