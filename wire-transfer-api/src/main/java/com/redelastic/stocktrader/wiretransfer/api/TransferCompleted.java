/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

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
    @NonNull String source;
    @NonNull String destination;
    @NonNull String amount;
}
