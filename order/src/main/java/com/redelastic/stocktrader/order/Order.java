package com.redelastic.stocktrader.order;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Order {
    @NonNull String orderId;
    @NonNull OrderDetails details;
}
