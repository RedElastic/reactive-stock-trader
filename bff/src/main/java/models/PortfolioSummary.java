package models;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.PSequence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

@Value
@Builder
public class PortfolioSummary {
    @NonNull String portfolioId;
    @NonNull String name;
    @NonNull BigDecimal funds;    
    
    BigDecimal totalStockValue;
    BigDecimal totalTradeCost;
    
    PSequence<EquityHolding> equities;
    PSequence<CompletedOrder> completedOrders;

    @Getter(lazy=true) private final BigDecimal returnValueTotal = calculateReturnValue();
    @Getter(lazy=true) private final BigDecimal returnPercentTotal = calculateReturnPercent();
    @Getter(lazy=true) private final boolean hasTransfers = funds.compareTo(BigDecimal.valueOf(0.0)) != 0 ? true : false;
    @Getter(lazy=true) private final boolean hasEquities = completedOrders.size() > 0 ? true : false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    @NonNull final Date asOf = new Date();

    private BigDecimal calculateReturnPercent() {
        int comparison = totalStockValue.compareTo(BigDecimal.valueOf(0.0));

        if (comparison > 0)
            return getReturnValueTotal().divide(totalTradeCost, 4, RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100));
        else
            return BigDecimal.valueOf(0.0);
    }

    private BigDecimal calculateReturnValue() {
        return totalStockValue.subtract(totalTradeCost);
    }
}
