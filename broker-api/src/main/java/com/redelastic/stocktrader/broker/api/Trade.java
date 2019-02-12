/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.broker.api;

import com.redelastic.stocktrader.TradeType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Trade {
    @NonNull String symbol;
    int shares;
    @NonNull TradeType tradeType;
    @NonNull BigDecimal sharePrice;
}
