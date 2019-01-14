package com.redelastic.stocktrader.broker.impl.order;

import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.broker.impl.trade.TradeService;
import com.redelastic.stocktrader.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class OrderRepositoryImpl implements OrderRepository  {

    private final Logger log = LoggerFactory.getLogger(OrderRepositoryImpl.class);
    private final PersistentEntityRegistry persistentEntities;
    private final TradeService tradeService;

    @Inject
    OrderRepositoryImpl(PersistentEntityRegistry persistentEntities,
                        TradeService tradeService) {
        this.persistentEntities = persistentEntities;
        this.tradeService = tradeService;
        persistentEntities.register(OrderEntity.class);
    }

    @Override
    public OrderProcessImpl get(String orderId) {
        return new OrderProcessImpl(
                persistentEntities.refFor(OrderEntity.class, orderId),
                tradeService);
    }


    public Source<Pair<OrderResult, Offset>, ?> orderResults(AggregateEventTag<OrderEvent> tag, Offset offset) {
        return persistentEntities.eventStream(tag, offset).filter(eventOffset ->
                eventOffset.first() instanceof OrderEvent.OrderFulfilled
        ).map(eventAndOffset -> {
            OrderEvent.OrderFulfilled fulfilled = (OrderEvent.OrderFulfilled)eventAndOffset.first();
            Order order = fulfilled.getOrder();
            Trade trade = fulfilled.getTrade();

            log.warn(String.format("Order %s fulfilled.", order.getOrderId()));

            OrderResult.OrderFulfilled completedOrder = OrderResult.OrderFulfilled.builder()
                    .orderId(order.getOrderId())
                    .portfolioId(order.getDetails().getPortfolioId())
                    .trade(trade)
                    .build();
            return Pair.create(completedOrder, eventAndOffset.second());
        });
    }
}
