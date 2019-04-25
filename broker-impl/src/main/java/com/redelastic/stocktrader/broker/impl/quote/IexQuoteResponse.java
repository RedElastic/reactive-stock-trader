/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.broker.impl.quote;

import lombok.Data;

import java.math.BigDecimal;

@Data
class IexQuoteResponse {
    String symbol;
    BigDecimal latestPrice;
}
