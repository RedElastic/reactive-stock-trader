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
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Log4j
public class WireTransferController extends Controller {

    private final WireTransferService wireTransferService;
    private final Form<TransferForm> transferForm;

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
                        val result = Json.newObject();
                        result.put("transferId", response.getId());
                        return result;
                    })
                    .thenApply(Results::created);
        }
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
