package com.redelastic.stocktrader.wiretransfer.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(PortfolioTransfer.Deposit.class),
        @JsonSubTypes.Type(PortfolioTransfer.Withdrawal.class)
})
public abstract class PortfolioTransfer {
    private PortfolioTransfer() {}

    @Value
    class Deposit extends PortfolioTransfer {
        @NonNull String transferId;
        @NonNull String portfolioId;
        @NonNull BigDecimal funds;
    }

    @Value
    class Withdrawal extends PortfolioTransfer {
        @NonNull String transferId;
        @NonNull String portfolioId;
        @NonNull BigDecimal funds;
    }
}
