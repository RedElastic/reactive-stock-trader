package com.redelastic.stocktrader.broker.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.Attributes;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.redelastic.stocktrader.broker.api.*;
import com.redelastic.stocktrader.broker.impl.order.OrderEntity;
import com.redelastic.stocktrader.broker.impl.order.OrderEvent;
import com.redelastic.stocktrader.broker.impl.order.OrderRepository;
import com.redelastic.stocktrader.broker.impl.quote.QuoteService;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.portfolio.api.OrderPlaced;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import com.redelastic.stocktrader.portfolio.api.order.Order;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Note: The only supported way within Lagom to publish to a topic is tracking
 * persistent entity state. This means we'll need to create persistent entities for
 * all our orderPlaced, even market orderPlaced which we might be tempted not to otherwise.
 */

public class BrokerServiceImpl implements BrokerService {

    private final Logger log = LoggerFactory.getLogger(BrokerServiceImpl.class);
    private final QuoteService quoteService;
    private final OrderRepository orderRepository;

    @Inject
    public BrokerServiceImpl(PersistentEntityRegistry persistentEntities,
                             QuoteService quoteService,
                             PortfolioService portfolioService,
                             OrderRepository orderRepository) {
        this.quoteService = quoteService;
        this.orderRepository = orderRepository;
        persistentEntities.register(OrderEntity.class);

        portfolioService.orderPlaced().subscribe().atLeastOnce(processPortfolioOrders());
    }

    @Override
    public ServiceCall<NotUsed, Quote> getQuote(String symbol) {

        return notUsed -> quoteService.getQuote(symbol);
    }

    @Override
    public ServiceCall<NotUsed, Optional<OrderSummary>> getOrderSummary(OrderId orderId) {

        return notUsed ->
                orderRepository
                        .get(orderId)
                        .getSummary();
    }

    @Override
    public Topic<OrderResult> orderResult() {
        return TopicProducer.taggedStreamWithOffset(OrderEvent.TAG.allTags(), orderRepository::orderResults);
    }


    private Flow<OrderPlaced, Done, NotUsed> processPortfolioOrders() {
        return Flow.<OrderPlaced>create()
                .log("orderPlaced")
                .addAttributes(Attributes.createLogLevels(
                        Attributes.logLevelInfo(), // onElement
                        Attributes.logLevelInfo(), // onFinish
                        Attributes.logLevelError()) // onFailure
                )
                // Note that order processing is asynchronous, so the parallelism parameter only limits how many
                // orders place at once before we get acknowledgement that they have been placed (which should be
                // essentially instant. It is not the maximum number of orders we can process concurrently.
                .mapAsync(10, this::processOrder);
    }

    private CompletionStage<Done> processOrder(OrderPlaced order) {
        return orderRepository.get(order.getOrderId()).placeOrder(order.getPortfolioId(), order.getOrderDetails());
    }

}