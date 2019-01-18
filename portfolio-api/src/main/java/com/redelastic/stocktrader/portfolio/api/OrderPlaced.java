package com.redelastic.stocktrader.portfolio.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.redelastic.stocktrader.order.OrderDetails;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class OrderPlaced {
    @NonNull String portfolioId;
    @NonNull String orderId;
    @NonNull OrderDetails orderDetails;

    @JsonCreator
    OrderPlaced(
            @JsonProperty("portfolioId") String portfolioId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("orderDetails") OrderDetails orderDetails) {
        this.portfolioId = portfolioId;
        this.orderId = orderId;
        this.orderDetails = orderDetails;
    }
}
