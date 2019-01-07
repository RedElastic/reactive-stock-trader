package com.redelastic.stocktrader.broker.api;

import com.redelastic.stocktrader.order.OrderType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Trade {
    String symbol;
    int shares;
    OrderType orderType;
    BigDecimal price;
}
