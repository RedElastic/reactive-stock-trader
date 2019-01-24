package com.redelastic.stocktrader.wiretransfer.api;

import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class Transfer {
    @NonNull String transferId;
    @NonNull Account sourceAccount;
    @NonNull Account destinationAccount;
    @NonNull BigDecimal funds;
}
