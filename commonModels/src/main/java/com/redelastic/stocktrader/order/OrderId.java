package com.redelastic.stocktrader.order;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class OrderId {
    @NonNull String id;

    public static OrderId newId() { return new OrderId(UUID.randomUUID().toString()); }
}
