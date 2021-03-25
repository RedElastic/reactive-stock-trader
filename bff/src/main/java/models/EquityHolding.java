package models;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

import com.redelastic.stocktrader.broker.api.Quote;

@Value
@Builder
public class EquityHolding {
    String symbol;
    Integer shares;
    BigDecimal currentValue;
    Quote quote;
}
