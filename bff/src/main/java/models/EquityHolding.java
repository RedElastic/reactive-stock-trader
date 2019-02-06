package models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class EquityHolding {
    @NonNull String symbol;
    int shares;
}
