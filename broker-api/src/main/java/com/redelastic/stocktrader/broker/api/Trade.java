package com.redelastic.stocktrader.broker.api;

import com.redelastic.stocktrader.order.OrderType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Trade {
    @NonNull String orderId;
    @NonNull String symbol;
    int shares;
    @NonNull OrderType orderType;
    @NonNull BigDecimal price;
}
