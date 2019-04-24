/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.wiretransfer.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.redelastic.stocktrader.PortfolioId;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(Account.Portfolio.class),
        @JsonSubTypes.Type(Account.SavingsAccount.class)
})
public abstract class Account {
    private Account() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Portfolio extends Account {
        PortfolioId portfolioId;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class SavingsAccount extends Account {
        String accountId;
    }
}
