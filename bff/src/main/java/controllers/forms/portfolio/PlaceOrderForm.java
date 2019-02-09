package controllers.forms.portfolio;

import com.redelastic.stocktrader.TradeType;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.order.OrderType.*;
import lombok.Data;

import java.math.BigDecimal;

@SuppressWarnings("WeakerAccess")
@Data
public class PlaceOrderForm {

    String symbol;
    int shares;
    Order order;
    OrderType orderType;
    BigDecimal limitPrice;

    public enum Order {
        buy,
        sell;

        public TradeType toTradeType() {
            switch (this) {
                case buy:
                    return TradeType.BUY;
                case sell:
                    return TradeType.SELL;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public enum OrderType {
        market,
        limit
    }

    public OrderDetails toOrderDetails() {
        com.redelastic.stocktrader.portfolio.api.order.OrderType orderType = null;
        switch (this.orderType) {
            case market:
                orderType = Market.INSTANCE;
                break;
            case limit:
                orderType = new Limit(limitPrice);
                break;
        }
        return OrderDetails.builder()
                .tradeType(this.getOrder().toTradeType())
                .symbol(this.getSymbol())
                .shares(this.getShares())
                .orderType(orderType)
                .build();
    }

}
