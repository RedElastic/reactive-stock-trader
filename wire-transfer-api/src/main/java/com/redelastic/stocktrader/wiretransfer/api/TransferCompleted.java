package com.redelastic.stocktrader.wiretransfer.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransferCompleted {
    @NonNull String id;
    @NonNull String status;
    @NonNull String dateTime;
    @NonNull String sourceType;
    @NonNull String sourceId;
    @NonNull String destinationType;
    @NonNull String destinationId;
    @NonNull String amount;
}
