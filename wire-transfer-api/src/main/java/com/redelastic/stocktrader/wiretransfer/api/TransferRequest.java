/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.wiretransfer.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.redelastic.stocktrader.TransferId;
import lombok.Builder;
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

    public abstract Account getAccount();

    public abstract <T> T visit(Visitor<T> visitor);

    public interface Visitor<T> {
        T visit(WithdrawlRequest withdrawlRequest);

        T visit(DepositRequest depositRequest);
    }

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    public static class WithdrawlRequest extends TransferRequest {
        TransferId transferId;
        Account account;
        BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    public static class DepositRequest extends TransferRequest {
        TransferId transferId;
        Account account;
        BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }
}
