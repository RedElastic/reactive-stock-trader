package com.redelastic.stocktrader.wiretransfer.api;

import lombok.EqualsAndHashCode;
import lombok.Value;

public abstract class Account {
    private Account() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Portfolio {
        String portfolioId;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class SavingsAccount {
        String accountId;
    }
}
