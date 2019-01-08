package com.redelastic.stocktrader.broker.impl.quote;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IexQuoteResponse {
    String symbol;
    BigDecimal latestPrice;
}
