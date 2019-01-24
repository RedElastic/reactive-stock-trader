package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.wiretransfer.api.Account;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import lombok.Value;
import lombok.experimental.Wither;

import java.math.BigDecimal;

@Value
@Wither
public class TransferState implements Jsonable {

    enum Status {
        GettingFunds,
        SendingFunds,
        Completed
    }

    Account source;
    Account destination;
    BigDecimal amount;
    Status status;

    public static TransferState from(Transfer transfer) {
        return new TransferState(
                transfer.getSourceAccount(),
                transfer.getDestinationAccount(),
                transfer.getFunds(),
                Status.GettingFunds
        );

    }

}
