/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.portfolio.api.*;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import com.redelastic.stocktrader.portfolio.api.PortfolioSummary;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.lightbend.lagom.javadsl.persistence.ReadSide;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.List;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import akka.stream.javadsl.Source;

import com.redelastic.stocktrader.PortfolioId;

import java.util.concurrent.CompletableFuture;

@Singleton
public class PortfolioServiceImpl implements PortfolioService {

    private final Logger log = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final PortfolioRepository portfolioRepository;
    private final CassandraSession db;

    @Inject
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository,
                                BrokerService brokerService, 
                                ReadSide readSide,
                                CassandraSession db) {
        this.portfolioRepository = portfolioRepository;
        this.db = db;

        // Listen for purchase order completions and send them to the corresponding portfolio
        brokerService.orderResult()
                .subscribe()
                .atLeastOnce(Flow.<OrderResult>create().mapAsync(1, this::handleOrderResult));
        
        readSide.register(PortfolioEventProcessor.class);
    }

    @Override
    public ServiceCall<OpenPortfolioDetails, PortfolioId> openPortfolio() {

        return portfolioRepository::open;
    }

    @Override
    public ServiceCall<NotUsed, Done> closePortfolio(PortfolioId portfolioId) {
        return notUsed ->
                portfolioRepository
                        .getRef(portfolioId)
                        .ask(PortfolioCommand.ClosePortfolio.INSTANCE);
    }

    @Override
    public ServiceCall<NotUsed, PortfolioView> getPortfolio(PortfolioId portfolioId) {
        return notUsed ->
                portfolioRepository
                        .get(portfolioId)
                        .view();
    }

    @Override
    public ServiceCall<NotUsed, PSequence<PortfolioSummary>> getAllPortfolios() {
        return request -> {
            CompletionStage<PSequence<PortfolioSummary>> result = db.selectAll(
                "SELECT portfolioId, name FROM portfolio_summary;").thenApply(rows -> {
                    List<PortfolioSummary> summary = rows.stream().map(row -> 
                        PortfolioSummary.builder()
                            .portfolioId(new PortfolioId(row.getString("portfolioId")))
                            .name(row.getString("name"))
                            .build())
                        .collect(Collectors.toList());
                    return TreePVector.from(summary);
                });
            return result;
        };
    }

    @Override
    public ServiceCall<FundsTransfer, Done> processTransfer(PortfolioId portfolioId) {
        val portfolioRef = portfolioRepository
                .getRef(portfolioId);
        return fundsTransfer ->
                fundsTransfer.visit(new FundsTransfer.Visitor<CompletionStage<Done>>() {
                    @Override
                    public CompletionStage<Done> visit(FundsTransfer.Deposit deposit) {
                        return portfolioRef.ask(new PortfolioCommand.ReceiveFunds(deposit.getFunds()));
                    }

                    @Override
                    public CompletionStage<Done> visit(FundsTransfer.Withdrawl withdrawl) {
                        return portfolioRef.ask(new PortfolioCommand.SendFunds(withdrawl.getFunds()));
                    }

                    @Override
                    public CompletionStage<Done> visit(FundsTransfer.Refund refund) {
                        return portfolioRef.ask(new PortfolioCommand.AcceptRefund(refund.getFunds(), refund.getTransferId()));
                    }
                });
    }

    @Override
    public ServiceCall<OrderDetails, OrderId> placeOrder(PortfolioId portfolioId) {
        return orderDetails -> {
            val orderId = OrderId.newId();
            return portfolioRepository
                    .get(portfolioId)
                    .placeOrder(orderId, orderDetails)
                    .thenApply(done -> orderId);
        };
    }

    private CompletionStage<Done> handleOrderResult(OrderResult orderResult) {
        PortfolioModel portfolio = portfolioRepository.get(orderResult.getPortfolioId());
        return orderResult.visit(new OrderResult.Visitor<CompletionStage<Done>>() {
            @Override
            public CompletionStage<Done> visit(OrderResult.Fulfilled orderFulfilled) {
                return portfolio.processTrade(orderFulfilled.getOrderId(), orderFulfilled.getTrade());
            }

            @Override
            public CompletionStage<Done> visit(OrderResult.Failed orderFailed) {
                return portfolio.orderFailed(orderFailed);
            }
        });
    }

    @Override
    public Topic<OrderPlaced> orderPlaced() {
        return TopicProducer.taggedStreamWithOffset(PortfolioEvent.TAG.allTags(), portfolioRepository::ordersStream);
    }


}
