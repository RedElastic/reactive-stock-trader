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
import com.redelastic.stocktrader.order.OrderDetails;
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
    public ServiceCall<OpenPortfolioRequest, String> openPortfolio() {
        return portfolioRepository::open;
    }

    @Override
    public ServiceCall<NotUsed, Done> liquidatePortfolio(String portfolioId) {
        return null;
    }

    @Override
    public ServiceCall<NotUsed, PortfolioView> getPortfolio(String portfolioId) {
        return notUsed ->
            portfolioRepository
                    .get(portfolioId)
                    .view();
    }

    @Override
    public ServiceCall<OrderDetails, Done> placeOrder(String portfolioId) {
        return orderDetails -> {
            String orderId = UUID.randomUUID().toString();
            return portfolioRepository
                    .get(portfolioId)
                    .placeOrder(Order.builder().orderId(orderId).details(orderDetails).build());
        };
    }

    /**
     * Illustrate synchronous inter-service communication pattern by making service call
     * to broker directly. The upside of this is that the call is made quicker (the
     * read-side processor used for the asynchronous topic based inter-service communication
     * introduces significant latency). The downside is that it couples the services in time,
     * if the broker service is currently unavailable the call will fail. The topic based
     * communication approach can handle such situations, resuming processing of orders once
     * the service is available. Since we're not dealing with a high-frequency trading
     * situation we can tolerate a few seconds latency between the trade being placed and
     * being processed, so we've used the asynchronous topic based approach here.
     * @return
     */
    private ServiceCall<OrderDetails, Done> placeOrderSync(String portfolioId) {
        return orderDetails -> {
            String orderId = UUID.randomUUID().toString();
            // We'll wait for the broker to acknowledge the order before completing since we won't
            // be able to recover (resubmit the order) if the broker is not available.
            Order order = Order.builder().orderId(orderId).details(orderDetails).build();
            return portfolioRepository
                    .get(portfolioId)
                    .placeOrder(order)
                    .thenCompose(done ->
                            brokerService.placeOrder().invoke(order));
        };
    }

    private CompletionStage<Done> handleOrderResult(OrderResult orderResult) {
        if (orderResult instanceof OrderResult.OrderCompleted) {
            Trade trade = ((OrderResult.OrderCompleted)orderResult).getTrade();
            return portfolioRepository
                    .get(orderResult.getPortfolioId())
                    .processTrade(trade);
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
