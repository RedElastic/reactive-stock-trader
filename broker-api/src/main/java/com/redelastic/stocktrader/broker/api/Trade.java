package com.redelastic.stocktrader.broker.api;

import com.redelastic.stocktrader.order.OrderId;
import com.redelastic.stocktrader.order.TradeType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Trade {
    @NonNull OrderId orderId;
    @NonNull String symbol;
    int shares;
    @NonNull TradeType tradeType;
    @NonNull BigDecimal price;
}
