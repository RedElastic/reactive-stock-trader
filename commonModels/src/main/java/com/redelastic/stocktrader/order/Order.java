package com.redelastic.stocktrader.order;

import com.redelastic.stocktrader.PortfolioId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class Order {
    @NonNull OrderId orderId;
    @NonNull PortfolioId portfolioId;
    @NonNull OrderDetails details;
}
