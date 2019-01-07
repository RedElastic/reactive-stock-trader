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
import com.redelastic.stocktrader.broker.impl.buyOrder.BuyOrderEntity;
import com.redelastic.stocktrader.broker.impl.buyOrder.BuyOrderEvent;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.order.OrderType;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class BrokerServiceImpl implements BrokerService {

    private Logger log = LoggerFactory.getLogger(BrokerServiceImpl.class);

    private final QuoteService quoteService;

    private final PersistentEntityRegistry persistentEntities;

    @Inject
    public BrokerServiceImpl(PersistentEntityRegistry persistentEntities,
                             QuoteService quoteService,
                             PortfolioService portfolioService) {
        this.quoteService = quoteService;
        this.persistentEntities = persistentEntities;
        persistentEntities.register(BuyOrderEntity.class);

        portfolioService.orders().subscribe().atLeastOnce(processPortfolioOrders());
    }

    @Override
    public ServiceCall<String, Quote> getQuote() {
        return quoteService::getQuote;
    }

    @Override
    public Topic<OrderResult> orderResults() {
        return TopicProducer.taggedStreamWithOffset(BuyOrderEvent.TAG.allTags(), this::orderResults);
    }


    private Source<Pair<OrderResult, Offset>, ?> orderResults(AggregateEventTag<BuyOrderEvent> tag, Offset offset) {
        return persistentEntities.eventStream(tag, offset).filter(eventOffset ->
                eventOffset.first() instanceof BuyOrderEvent.Fulfilled
        ).map(eventAndOffset -> {
            BuyOrderEvent.Fulfilled fulfilled = (BuyOrderEvent.Fulfilled)eventAndOffset.first();

            Trade trade = Trade.builder()
                    .orderType(OrderType.BUY)
                    .symbol(fulfilled.getOrder().getSymbol())
                    .shares(fulfilled.getOrder().getShares())
                    .build();
            OrderResult.OrderCompleted completedOrder = OrderResult.OrderCompleted.builder()
                    .orderId(fulfilled.getOrderId())
                    .portfolioId(fulfilled.getOrder().getPortfolioId())
                    .trade(trade)
                    .build();
            return Pair.create(completedOrder, eventAndOffset.second());
        });
    }

    private Flow<Order, Done, NotUsed> processPortfolioOrders() {
        return Flow.<Order>create()
                .mapAsync(1, this::processOrder);
    }

    private CompletionStage<Done> processOrder(Order order) {
        log.warn(String.format("Broker received order %s from portfolio %s",
                order.getOrderId(),
                order.getPortfolioId()));
        return CompletableFuture.completedFuture(Done.getInstance());
    }

}
