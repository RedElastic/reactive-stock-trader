/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package controllers;

import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.wiretransfer.api.Account;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import controllers.forms.transfer.TransferForm;
import lombok.extern.log4j.Log4j;
import lombok.val;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.libs.F.Either;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import play.mvc.*;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F;
import akka.NotUsed;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import java.util.Arrays;

@Log4j
public class WireTransferController extends Controller {

    private final WireTransferService wireTransferService;
    private final Form<TransferForm> transferForm;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger("controllers.WireTransferController");

    @Inject
    private WireTransferController(WireTransferService wireTransferService,
                                   FormFactory formFactory) {
        this.wireTransferService = wireTransferService;
        this.transferForm = formFactory.form(TransferForm.class);
    }

    public CompletionStage<Result> transfer() {
        Form<TransferForm> form = transferForm.bindFromRequest();
        if (form.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(form.errorsAsJson()));
        } else {
            Transfer transfer = populateTransfer(form.get());
            return wireTransferService
                .transferFunds()
                .invoke(transfer)
                .thenApply(response -> {
                    val result = Json.newObject()
                            .put("transferId", response.getId());
                    return Results.status(Http.Status.ACCEPTED, result);
                });
        }
    }

    public CompletionStage<Result> getAllTransfersFor(String portfolioId) {
        val transfers = wireTransferService
            .getAllTransactionsFor(portfolioId)
            .invoke();

        return transfers
            .thenApply(Json::toJson)
            .thenApply(Results::ok);
    }

    public WebSocket ws() {
        return WebSocket.Json.acceptOrResult(req -> {
            return wireTransferService
                .transferStream()
                .invoke()
                .thenApply(source -> {
                    return F.Either.Right(Flow.fromSinkAndSourceCoupled(Sink.ignore(), source));
                });
        });
    }

    private Transfer populateTransfer(TransferForm form) {
        Account sourceAccount = getAccount(form.getSourceType(), form.getSourceId());
        Account destinationAccount = getAccount(form.getDestinationType(), form.getDestinationId());
        return Transfer.builder()
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .funds(form.getAmount())
                .build();
    }

    private Account getAccount(TransferForm.AccountType accountType, String accountId) {
        switch (accountType) {
            case portfolio:
                return new Account.Portfolio(new PortfolioId(accountId));
            case savings:
                return new Account.SavingsAccount(accountId);
            default:
                log.error(accountType);
                throw new IllegalStateException();
        }
    }



}
