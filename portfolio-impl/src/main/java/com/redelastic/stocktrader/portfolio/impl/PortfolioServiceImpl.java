package com.redelastic.stocktrader.portfolio.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.broker.api.BrokerService;
import com.redelastic.stocktrader.broker.api.OrderResult;
import com.redelastic.stocktrader.portfolio.api.order.OrderDetails;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.portfolio.api.*;
import com.redelastic.stocktrader.wiretransfer.api.Account;
import com.redelastic.stocktrader.wiretransfer.api.TransferRequest;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

@Singleton
public class PortfolioServiceImpl implements PortfolioService {

    private final Logger log = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final PortfolioRepository portfolioRepository;

    @Inject
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository,
                                BrokerService brokerService,
                                WireTransferService wireTransferService) {
        this.portfolioRepository = portfolioRepository;

        // Listen for purchase order completions and send them to the corresponding portfolio
        brokerService.orderResult()
                .subscribe()
                .atLeastOnce(Flow.<OrderResult>create().mapAsync(1, this::handleOrderResult));
        // Note: Our order entity logic handles duplicate orderPlaced, hence at least once semantics work.

        wireTransferService
                .transferRequest()
                .subscribe()
                .atLeastOnce(
                        Flow.<TransferRequest>create()
                                .filter(request -> request.getAccount() instanceof Account.Portfolio)
                                .mapAsync(1, this::processTransferRequests)
                );
    }

    private CompletionStage<Done> processTransferRequests(TransferRequest request) {
        return request.visit(new TransferRequest.Visitor<CompletionStage<Done>>() {
            @Override
            public CompletionStage<Done> visit(TransferRequest.WithdrawlRequest withdrawlRequest) {
                return handleWithdrawl(withdrawlRequest);
            }

            @Override
            public CompletionStage<Done> visit(TransferRequest.DepositRequest depositRequest) {
                return handleDeposit(depositRequest);
            }
        });
    }

    private CompletionStage<Done> handleWithdrawl(TransferRequest.WithdrawlRequest request) {
        PortfolioId portfolioId = ((Account.Portfolio)request.getAccount()).getPortfolioId();
        return portfolioRepository
                .getRef(portfolioId)
                .ask(PortfolioCommand.SendFunds.builder().amount(request.getAmount()).build());
    }

    private CompletionStage<Done> handleDeposit(TransferRequest.DepositRequest request) {
        PortfolioId portfolioId = ((Account.Portfolio)request.getAccount()).getPortfolioId();
        return portfolioRepository
                .getRef(portfolioId)
                .ask(PortfolioCommand.ReceiveFunds.builder().amount(request.getAmount()).build());
    }

    @Override
    public ServiceCall<OpenPortfolioDetails, PortfolioId> openPortfolio() {

        return portfolioRepository::open;
    }

    @Override
    public ServiceCall<NotUsed, Done> liquidatePortfolio(PortfolioId portfolioId) {
        return null;
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
