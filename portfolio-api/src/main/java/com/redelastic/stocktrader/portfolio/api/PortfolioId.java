package com.redelastic.stocktrader.portfolio.api;

import lombok.NonNull;
import lombok.Value;

@Value
public class PortfolioId {
    @NonNull
    String id;

    public String asString() {
        return id;
    }
}
