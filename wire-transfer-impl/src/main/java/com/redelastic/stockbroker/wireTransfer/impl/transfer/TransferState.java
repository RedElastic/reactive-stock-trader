package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.wiretransfer.api.Account;
import com.redelastic.stocktrader.wiretransfer.api.Transfer;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.math.BigDecimal;

@Value
@Wither
public class TransferState implements Jsonable {

    enum Status {
        GettingFunds,
        GettingFundsFailed,
        SendingFunds,
        SendingFundsFailed,
        ConfirmingDelivery,
        DeliveryConfirmed,
        RefundSent,
        RefundDelivered
    }

    @NonNull Account source;
    @NonNull Account destination;
    @NonNull BigDecimal amount;
    @NonNull Status status;

    public static TransferState from(Transfer transfer) {
        return new TransferState(
                transfer.getSourceAccount(),
                transfer.getDestinationAccount(),
                transfer.getFunds(),
                Status.GettingFunds
        );

    }

}
