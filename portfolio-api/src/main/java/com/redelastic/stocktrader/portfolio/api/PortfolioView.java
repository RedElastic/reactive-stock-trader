package com.redelastic.stocktrader.portfolio.api;

import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.PSequence;

import java.math.BigDecimal;

@Value
@Builder
public class PortfolioView {

    @NonNull PortfolioId portfolioId;

    @NonNull String name;

    @NonNull BigDecimal funds;

    @NonNull PSequence<Holding> holdings;

    @NonNull PSequence<OrderId> completedOrders;
}
