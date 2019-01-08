package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.broker.api.Trade;
import com.redelastic.stocktrader.order.Order;
import com.redelastic.stocktrader.portfolio.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class PortfolioServiceImpl implements PortfolioService {

    private final Logger log = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final PortfolioRepository portfolioRepository;
    private final BrokerService brokerService;

    @Inject
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository,
                                BrokerService brokerService) {
        this.portfolioRepository = portfolioRepository;
        this.brokerService = brokerService;

        // Listen for purchase order completions and send them to the corresponding portfolio
        brokerService.orderResults()
                .subscribe()
                .atLeastOnce(Flow.<OrderResult>create().mapAsync(1, this::handleOrderResult));
        // TODO: deal with duplicates
    }

    @Override
    public ServiceCall<NewPortfolioRequest, String> openPortfolio() { return portfolioRepository::open; }

    @Override
    public ServiceCall<NotUsed, Done> liquidatePortfolio(String portfolioId) {
        return null;
    }

    @Override
    public ServiceCall<NotUsed, PortfolioView> getPortfolio(String portfolioId) {
        return notUsed ->
            portfolioRepository.get(portfolioId);
    }

    @Override
    public ServiceCall<Order, Done> placeOrder(String portfolioId) {
        return order ->
                portfolioRepository.getRef(portfolioId)
                    .ask(new PortfolioCommand.PlaceOrder(order.withOrderId(UUID.randomUUID().toString())));
    }

    /**
     * Illustrate synchronous interservice communication pattern by making service call
     * to broker directly.
     * @return
     */
    public ServiceCall<Order, Done> placeOrderSync(String portfolioId) {
        return order -> {
            String orderId = UUID.randomUUID().toString();
            return portfolioRepository.getRef(portfolioId)
                    .ask(new PortfolioCommand.PlaceOrder(order.withOrderId(orderId)))
                    .thenCompose(done -> brokerService.placeOrder().invoke(order));
        };
    }


    private CompletionStage<Done> handleOrderResult(OrderResult orderResult) {
        if (orderResult instanceof OrderResult.OrderCompleted) {
            Trade trade = ((OrderResult.OrderCompleted)orderResult).getTrade();
            return portfolioRepository.getRef(orderResult.getPortfolioId())
                    .ask(new PortfolioCommand.CompleteTrade(trade));
        } else {
            // TODO: handle this
            return CompletableFuture.completedFuture(Done.getInstance());
        }
    }

    @Override
    public Topic<Order> orders() {
        return TopicProducer.taggedStreamWithOffset(PortfolioEvent.TAG.allTags(), portfolioRepository::ordersStream);
    }


}
