package com.redelastic.wiretransfer.api;

import lombok.Value;

@Value
public class Holding {
    String symbol;

    int shares;
}
