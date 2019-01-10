package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.broker.impl.trade.TradeService;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class OrderProcessImpl implements OrderProcess {

    private final PersistentEntityRef<OrderCommand> orderEntity;
    private final TradeService tradeService;

    OrderProcessImpl(PersistentEntityRef<OrderCommand> orderEntity,
                     TradeService tradeService) {
        this.orderEntity = orderEntity;
        this.tradeService = tradeService;
    }

    public CompletionStage<Done> placeOrder(OrderDetails order) {
        CompletionStage<Order> placeOrder = orderEntity.ask(new OrderCommand.PlaceOrder(order));
        placeOrder
                .thenCompose(tradeService::placeOrder)
                .thenCompose(orderResult ->
                        orderEntity.ask(new OrderCommand.Complete(orderResult)));

        // Note that our service call responds with Done after the PlaceOrder command is accepted, it does not
        // wait for the order to be fulfilled (which, in general, may require some time).
        return placeOrder.thenApply(o -> Done.getInstance());
    }

    public CompletionStage<Optional<OrderStatus>> getStatus() {
        return orderEntity.ask(OrderCommand.GetStatus.INSTANCE);
    }
}
