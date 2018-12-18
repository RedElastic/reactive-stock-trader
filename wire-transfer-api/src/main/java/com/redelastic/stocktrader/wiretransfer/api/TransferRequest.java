package com.redelastic.stocktrader.wiretransfer.api;

import lombok.Value;

@Value
public class TransferRequest {
    Account source;
    Account destination;
}
