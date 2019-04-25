/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package controllers.forms.portfolio;

import com.redelastic.stocktrader.TradeType;
import lombok.Data;

@SuppressWarnings("WeakerAccess")
@Data
public class PlaceOrderForm {

    String symbol;
    int shares;
    Order order;

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
}
