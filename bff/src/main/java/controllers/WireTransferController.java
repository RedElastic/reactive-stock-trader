package controllers;

import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.wiretransfer.api.Account;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public class WireTransferController extends Controller {

    private final WireTransferService wireTransferService;

    @Inject
    private WireTransferController(WireTransferService wireTransferService,
                                   FormFactory formFactory) {
        this.wireTransferService = wireTransferService;

    }

    public CompletionStage<Result> transfer(String portfolioId) {
        Transfer transfer = Transfer.builder()
                .sourceAccount(new Account.SavingsAccount("123'"))
                .destinationAccount(new Account.Portfolio(new PortfolioId(portfolioId)))
                .funds(BigDecimal.valueOf(100))
                .build();

        return wireTransferService
                .transferFunds()
                .invoke(transfer)
                .thenApply(Json::toJson)
                .thenApply(Results::ok);
    }


}
