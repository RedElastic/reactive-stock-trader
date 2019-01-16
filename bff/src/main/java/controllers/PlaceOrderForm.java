package controllers;

import com.redelastic.stocktrader.order.OrderType;
import lombok.Data;

@Data
class PlaceOrderForm {

    String symbol;

    int shares;

    OrderType orderType;
}
