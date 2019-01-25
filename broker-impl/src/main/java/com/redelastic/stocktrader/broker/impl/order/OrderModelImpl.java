package com.redelastic.stocktrader.broker.impl.order;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.OrderStatus;
import com.redelastic.stocktrader.broker.impl.trade.TradeService;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class OrderModelImpl implements OrderModel {

    private final Logger log = LoggerFactory.getLogger(OrderModelImpl.class);

    private final PersistentEntityRef<OrderCommand> orderEntity;
    private final TradeService tradeService;

    OrderModelImpl(PersistentEntityRef<OrderCommand> orderEntity,
                   TradeService tradeService) {
        this.orderEntity = orderEntity;
        this.tradeService = tradeService;
    }

    public CompletionStage<Done> placeOrder(String portfolioId, OrderDetails orderDetails) {
        CompletionStage<Order> placeOrder = orderEntity.ask(new OrderCommand.PlaceOrder(portfolioId, orderDetails));

        // This is the process that will progress our order through to completion.
        // FIXME: if the service is interrupted before this is completed we will not reattempt.
        //  Consider the implications.
        placeOrder.thenAccept(order ->
                tradeService.placeOrder(order)
                .exceptionally(ex -> {
                    log.info(String.format("Order %s failed, %s.", orderEntity.entityId(), ex.toString()));
                    return new OrderResult.Failed(order.getPortfolioId(), orderEntity.entityId());
                })
                .thenAccept(orderResult -> {
                    log.info(String.format("Order %s completing.", orderEntity.entityId()));
                    orderEntity.ask(new OrderCommand.Complete(orderResult));
                })
        );
        // Note that our service call responds with Done after the PlaceOrder command is accepted, it does not
        // wait for the order to be fulfilled (which, in general, may require some time).
        return placeOrder.thenApply(o -> Done.getInstance());
    }

    public CompletionStage<Optional<OrderStatus>> getStatus() {
        return orderEntity.ask(OrderCommand.GetStatus.INSTANCE);
    }
}
