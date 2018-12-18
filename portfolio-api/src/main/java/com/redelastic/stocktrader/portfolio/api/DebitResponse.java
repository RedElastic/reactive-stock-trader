package com.redelastic.stocktrader.portfolio.api;

public interface DebitResponse {

    enum Successful implements DebitResponse {
        INSTANCE
    }

    enum InsufficientFunds implements DebitResponse {
        INSTANCE
    }
}
