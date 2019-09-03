package com.redelastic.stocktrader.portfolio.api.order;

import com.redelastic.stocktrader.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class OrderDetails {
    @NonNull String symbol;
    int shares;
    @NonNull TradeType tradeType;
    @NonNull OrderType orderType;
}
