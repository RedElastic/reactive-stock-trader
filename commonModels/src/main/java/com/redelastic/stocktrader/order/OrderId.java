package com.redelastic.stocktrader.order;

import lombok.NonNull;
import lombok.Value;

@Value
public class OrderId {
    @NonNull String id;
}
