package com.redelastic.stocktrader;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class TransferId {
    @NonNull String id;

    public static TransferId newId() { return new TransferId(UUID.randomUUID().toString()); }
}
