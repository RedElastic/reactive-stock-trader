/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.broker.api;

import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.TradeType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class OrderSummary {

    @NonNull OrderId orderId;
    @NonNull PortfolioId portfolioId;
    @NonNull TradeType tradeType;
    @NonNull String symbol;
    int shares;
    @NonNull OrderStatus status;
}
