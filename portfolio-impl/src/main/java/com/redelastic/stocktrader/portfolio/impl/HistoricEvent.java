package com.redelastic.stocktrader.portfolio.impl;

import lombok.Value;

@Value
public class HistoricEvent {
    PortfolioEvent event;
    PortfolioState state;
}
