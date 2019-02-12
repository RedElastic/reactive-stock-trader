/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import com.redelastic.stocktrader.wiretransfer.api.Account;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransferDetails {

    @NonNull Account source;
    @NonNull Account destination;
    @NonNull BigDecimal amount;
}
