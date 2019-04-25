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
public class Transfer {
    @NonNull Account sourceAccount;
    @NonNull Account destinationAccount;
    @NonNull BigDecimal funds;
}
