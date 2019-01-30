package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import com.lightbend.lagom.serialization.Jsonable;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Wither
public class TransferState implements Jsonable {

    enum Status {
        FundsRequested,
        UnableToSecureFunds,
        FundsSent,
        DeliveryConfirmed,
        RefundSent,
        RefundDelivered
    }

    @NonNull TransferDetails transferDetails;
    @NonNull Status status;

    public static TransferState from(TransferDetails transfer) {
        return new TransferState(
                TransferDetails.builder()
                        .source(transfer.getSource())
                        .destination(transfer.getDestination())
                        .amount(transfer.getAmount())
                        .build(),
                Status.FundsRequested
        );

    }

}
