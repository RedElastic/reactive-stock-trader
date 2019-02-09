package models;

import com.redelastic.stocktrader.TradeType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Order {
    @NonNull String orderId;
    String symbol;
    Integer shares;
    BigDecimal price;
    TradeType tradeType;
}
