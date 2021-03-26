package models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

import com.redelastic.stocktrader.broker.api.DetailedQuote;

@Value
@Builder
public class EquityHolding {
    @NonNull String symbol;
    @NonNull Integer shares;
    @NonNull BigDecimal currentValue;
    @NonNull DetailedQuote detailedQuote;
}
