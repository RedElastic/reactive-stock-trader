package models;

import com.redelastic.stocktrader.TradeType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CompletedOrder {
    @NonNull String orderId;
    @NonNull String symbol;
    @NonNull Integer shares;
    @NonNull BigDecimal price;
    @NonNull TradeType tradeType;
}
