package com.redelastic.stocktrader.broker.impl.buyOrder;

import com.redelastic.stocktrader.order.Order;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Wither
public class BuyOrderState {
    enum Status {
        Ready,
        Fulfilled
    }

    Order order;
    Status status;
}
