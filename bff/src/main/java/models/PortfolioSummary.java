package models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.PSequence;

import java.math.BigDecimal;

@Value
@Builder
public class PortfolioSummary {
    @NonNull String portfolioId;

    @NonNull String name;

    @NonNull BigDecimal funds;

    PSequence<EquityHolding> equities;

    PSequence<Order> completedOrders;
    PSequence<Order> pendingOrders;
}
