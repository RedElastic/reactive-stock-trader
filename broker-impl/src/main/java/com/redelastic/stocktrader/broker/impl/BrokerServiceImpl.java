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
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.broker.api.*;
import com.redelastic.stocktrader.broker.impl.order.OrderEntity;
import com.redelastic.stocktrader.broker.impl.order.OrderEvent;
import com.redelastic.stocktrader.broker.impl.order.OrderRepositoryImpl;
import com.redelastic.stocktrader.broker.impl.quote.QuoteService;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Note: The only supported way within Lagom to publish to a topic is tracking
 * persistent entity state. This means we'll need to create persistent entities for
 * all our orders, even market orders which we might be tempted not to otherwise.
 */

public class BrokerServiceImpl implements BrokerService {

    private final Logger log = LoggerFactory.getLogger(BrokerServiceImpl.class);
    private final QuoteService quoteService;
    private final OrderRepositoryImpl orderRepository;

    @Inject
    public BrokerServiceImpl(PersistentEntityRegistry persistentEntities,
                             QuoteService quoteService,
                             PortfolioService portfolioService,
                             OrderRepositoryImpl orderRepository) {
        this.quoteService = quoteService;
        this.orderRepository = orderRepository;
        persistentEntities.register(OrderEntity.class);

        portfolioService.orders().subscribe().atLeastOnce(processPortfolioOrders());
    }

    @Override
    public ServiceCall<NotUsed, Quote> getQuote(String symbol) {

        return notUsed -> quoteService.getQuote(symbol);
    }

    @Override
    public ServiceCall<NotUsed, Optional<OrderStatus>> getOrderStatus(String orderId) {

        return notUsed ->
                orderRepository
                .get(orderId)
                .getStatus();
    }

    @Override
    public ServiceCall<Order, Done> placeOrder() {
        return order ->
                orderRepository
                    .placeOrder(order);
    }

    @Override
    public Topic<OrderResult> orderResults() {
        return TopicProducer.taggedStreamWithOffset(OrderEvent.TAG.allTags(), orderRepository::orderResults);
    }


    private Flow<Order, Done, NotUsed> processPortfolioOrders() {
        return Flow.<Order>create()
                .mapAsync(1, this.placeOrder()::invoke);
    }

}