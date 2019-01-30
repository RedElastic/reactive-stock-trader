package com.redelastic.stocktrader.portfolio.api;

import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.order.OrderId;
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
