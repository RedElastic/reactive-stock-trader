package com.redelastic.stocktrader.portfolio.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(FundsTransfer.FundsDeposited.class),
        @JsonSubTypes.Type(FundsTransfer.FundsDebited.class)
})
public abstract class FundsTransfer {

    private FundsTransfer() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class FundsDeposited {
        @NonNull String transferId;
        @NonNull String portfolioId;
        @NonNull BigDecimal funds;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class FundsDebited {
        @NonNull String transferId;
        @NonNull String portfolioId;
        @NonNull BigDecimal funds;
    }

}
