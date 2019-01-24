package com.redelastic.stocktrader.wiretransfer.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(TransferRequest.WithdrawlRequest.class),
        @JsonSubTypes.Type(TransferRequest.DepositRequest.class)
})
public abstract class TransferRequest {
    private TransferRequest() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class WithdrawlRequest extends TransferRequest {
        Account account;
        BigDecimal amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DepositRequest extends TransferRequest {
        Account account;
        BigDecimal amount;
    }
}
