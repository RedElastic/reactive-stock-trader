package controllers.forms.portfolio;

import com.redelastic.stocktrader.order.TradeType;
import lombok.Data;

@SuppressWarnings("WeakerAccess")
@Data
public class PlaceOrderForm {

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

    String symbol;

    int shares;

    Order order;
}
