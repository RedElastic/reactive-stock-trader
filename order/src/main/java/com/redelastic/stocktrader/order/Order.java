package com.redelastic.stocktrader.order;

import lombok.NonNull;
import lombok.Value;

@Value
public class Order {
    @NonNull String orderId;
    @NonNull OrderDetails details;
}
