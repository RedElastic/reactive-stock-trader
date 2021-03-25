package com.redelastic.stocktrader.broker.impl.quote;

import java.math.BigDecimal;

import lombok.Data;

@Data
class IexQuoteResponse {
    String symbol;
    BigDecimal latestPrice;
}
