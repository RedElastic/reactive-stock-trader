package com.redelastic.stocktrader.portfolio.api;

import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
class MoneyTransfer {
    @NonNull String portfolioId;
    @NonNull BigDecimal funds;
}
