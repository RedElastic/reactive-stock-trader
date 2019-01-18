package com.redelastic.stocktrader.portfolio.api;

import com.redelastic.stocktrader.order.OrderDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class OrderPlaced {
    @NonNull String portfolioId;
    @NonNull String orderId;
    @NonNull OrderDetails orderDetails;
}
