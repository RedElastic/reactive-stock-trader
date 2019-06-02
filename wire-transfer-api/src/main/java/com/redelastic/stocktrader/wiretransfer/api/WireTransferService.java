/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.wiretransfer.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.redelastic.stocktrader.TransferId;
import org.pcollections.PSequence;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.*;


/**
 * We won't try to model arbitrary account to account transfers.
 */
public interface WireTransferService extends Service {

    String PORTFOLIO_TRANSFER_TOPIC_ID = "WireTransfer-PortfolioTransfer";
    String TRANSFER_REQUEST_TOPIC_ID = "WireTransfer-TransferRequest";

    ServiceCall<Transfer, TransferId> transferFunds();

    ServiceCall<NotUsed, PSequence<TransactionSummary>> getAllTransactionsFor(String portfolioId);

    Topic<TransferCompleted> completedTransfers();

    Topic<TransferRequest> transferRequest();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("wire-transfer").withCalls(
            call(this::transferFunds),
            restCall(Method.GET, "/api/transfer/:portfolioId", this::getAllTransactionsFor)
        )
        .withTopics(
            topic(PORTFOLIO_TRANSFER_TOPIC_ID, this::completedTransfers),
            topic(TRANSFER_REQUEST_TOPIC_ID, this::transferRequest)
        );
        // @formatter:on
    }
}
