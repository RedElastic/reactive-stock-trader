package controllers.forms;

import com.redelastic.stocktrader.order.TradeType;
import lombok.Data;

@SuppressWarnings("WeakerAccess")
@Data
public class PlaceOrderForm {

    String symbol;

    int shares;

    TradeType tradeType;
}
