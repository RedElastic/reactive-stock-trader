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

    @NonNull
    BigDecimal funds;

    @NonNull PSequence<EquityHolding> equities;
}
