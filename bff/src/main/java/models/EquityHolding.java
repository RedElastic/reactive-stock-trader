package models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class EquityHolding {
    @NonNull String symbol;
    int shares;
    BigDecimal sharePrice;
}
