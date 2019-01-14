package com.redelastic.stocktrader.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class Order {
    @NonNull String orderId;
    @NonNull OrderDetails details;
}
