package com.redelastic.stocktrader.portfolio.api;

import lombok.NonNull;
import lombok.Value;

@Value
public class OpenPortfolioDetails {
    @NonNull String name;
}
