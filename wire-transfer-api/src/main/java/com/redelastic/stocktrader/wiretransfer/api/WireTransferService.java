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
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * We won't try to model arbitrary account to account transfers.
 */
public interface WireTransferService extends Service {

    String TRANSFER_REQUEST_TOPIC_ID = "transfers";

    ServiceCall<Transfer, TransferId> transferFunds();

    ServiceCall<NotUsed, PSequence<TransactionSummary>> getAllTransactionsFor(String portfolioId);

    ServiceCall<NotUsed, Source<String, ?>> transferStream();

    Topic<TransferRequest> transferRequest();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("reactivestock-wiretransfer").withCalls(
            call(this::transferFunds),
            call(this::transferStream),
            restCall(Method.GET, "/api/transfer/:portfolioId", this::getAllTransactionsFor)            
        )
        .withTopics(
            topic(TRANSFER_REQUEST_TOPIC_ID, this::transferRequest)
        )
        .withAutoAcl(true);
        // @formatter:on
    }
}
