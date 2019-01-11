package com.redelastic.stocktrader.portfolio.impl;

import lombok.NonNull;
import lombok.Value;


@Value
class Holding {
    @NonNull String symbol;

    int shareCount;
}
