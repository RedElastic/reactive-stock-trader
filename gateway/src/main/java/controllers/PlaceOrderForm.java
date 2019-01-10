package controllers;

import com.redelastic.stocktrader.order.OrderType;
import lombok.Data;
import lombok.NonNull;

@Data
public class PlaceOrderForm {

    String symbol;

    int shares;

    OrderType orderType;
}
