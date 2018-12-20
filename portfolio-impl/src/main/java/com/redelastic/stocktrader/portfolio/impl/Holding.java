package com.redelastic.stocktrader.portfolio.impl;

import lombok.Value;


@Value
public class Holding {
    String symbol;

    int shareCount;
}
