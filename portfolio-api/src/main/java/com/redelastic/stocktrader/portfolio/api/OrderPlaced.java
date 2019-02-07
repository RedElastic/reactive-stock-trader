package com.redelastic.stocktrader.portfolio.api;

import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class OrderPlaced {
    @NonNull PortfolioId portfolioId;
    @NonNull OrderId orderId;
    @NonNull OrderDetails orderDetails;
}
