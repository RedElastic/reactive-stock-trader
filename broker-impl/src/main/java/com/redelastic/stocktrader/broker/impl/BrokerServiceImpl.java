package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.broker.api.*;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Note: The only supported way within Lagom to publish to a topic is tracking
 * persistent entity state. This means we'll need to create persistent entities for
 * all our orders, even market orders which we might be tempted not to otherwise.
 */

public class BrokerServiceImpl implements BrokerService {

    private final Logger log = LoggerFactory.getLogger(BrokerServiceImpl.class);

    private final QuoteService quoteService;

    private final TradeService tradeService;

    private final PersistentEntityRegistry persistentEntities;

    @Inject
    public BrokerServiceImpl(PersistentEntityRegistry persistentEntities,
                             QuoteService quoteService,
                             PortfolioService portfolioService,
                             TradeService tradeService) {
        this.quoteService = quoteService;
        this.persistentEntities = persistentEntities;
        this.tradeService = tradeService;
        persistentEntities.register(OrderEntity.class);

        portfolioService.orders().subscribe().atLeastOnce(processPortfolioOrders());
    }

    @Override
    public ServiceCall<NotUsed, Quote> getQuote(String symbol) {
        return notUsed -> quoteService.getQuote(symbol);
    }

    @Override
    public ServiceCall<NotUsed, OrderStatus> getOrderStatus(String orderId) {
        return null;
    }

    @Override
    public ServiceCall<Order, Done> placeOrder() {
        return order -> {
            PersistentEntityRef<OrderCommand> orderEntity = persistentEntities.refFor(OrderEntity.class, order.getOrderId());
            CompletionStage<Done> placeOrder = orderEntity.ask(new OrderCommand.PlaceOrder(order));

            log.warn(String.format("Broker placing order %s", order.getOrderId()));
            log.warn(String.format("Broker placing order %s", order.toString()));

            placeOrder
                    .thenCompose(done -> tradeService.placeOrder(order))
                    .thenApply(orderResult -> {
                        log.warn(String.format("Trade completed for order %s", order.getOrderId()));
                        return orderResult;
                    })
                    .thenCompose(orderResult ->
                            orderEntity.ask(new OrderCommand.Complete(orderResult)));

            // Note that our service call responds with Done after the PlaceOrder command is accepted, it does not
            // wait for the order to be fulfilled (which, in general, may require some time).
            return placeOrder;
        };
    }

    @Override
    public Topic<OrderResult> orderResults() {
        return TopicProducer.taggedStreamWithOffset(OrderEvent.TAG.allTags(), this::orderResults);
    }


    private Source<Pair<OrderResult, Offset>, ?> orderResults(AggregateEventTag<OrderEvent> tag, Offset offset) {
        return persistentEntities.eventStream(tag, offset).filter(eventOffset ->
                eventOffset.first() instanceof OrderEvent.OrderFulfilled
        ).map(eventAndOffset -> {
            OrderEvent.OrderFulfilled fulfilled = (OrderEvent.OrderFulfilled)eventAndOffset.first();
            Order order = fulfilled.getOrder();
            Trade trade = fulfilled.getTrade();

            log.warn(String.format("Order %s fulfilled.", order.getOrderId()));

            OrderResult.OrderCompleted completedOrder = OrderResult.OrderCompleted.builder()
                    .orderId(order.getOrderId())
                    .portfolioId(order.getPortfolioId())
                    .trade(trade)
                    .build();
            return Pair.create(completedOrder, eventAndOffset.second());
        });
    }

    private Flow<Order, Done, NotUsed> processPortfolioOrders() {
        return Flow.<Order>create()
                .mapAsync(1, this.placeOrder()::invoke);
    }

}