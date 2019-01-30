package models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.PSequence;
import services.quote.ValuedHolding;

import java.math.BigDecimal;

@Value
@Builder
public class PortfolioView {

    @NonNull String portfolioId;

    @NonNull String name;

    @NonNull BigDecimal funds;

    @NonNull PSequence<ValuedHolding> holdings;
}
