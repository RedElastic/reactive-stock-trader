package com.redelastic.wiretransfer.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.concurrent.Immutable;

@Value
@Immutable
@JsonDeserialize
public final class NewPortfolioRequest {
    @NonNull
    public final String owner;

    @NonNull
    public final String linkedAccount;
}
