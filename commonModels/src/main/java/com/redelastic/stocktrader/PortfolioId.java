package com.redelastic.stocktrader;

import com.redelastic.stocktrader.order.OrderId;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class PortfolioId {
    @NonNull String id;

    public static PortfolioId newId() { return new PortfolioId(UUID.randomUUID().toString()); }
}
