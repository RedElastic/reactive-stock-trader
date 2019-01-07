package com.redelastic.stocktrader.portfolio.api;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class MoneyTransfer {
    String portfolioId;
    BigDecimal funds;
}
