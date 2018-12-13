package com.redelastic.wiretransfer.api;

import lombok.Value;

import java.util.List;

@Value
public class PortfolioView {

    String portfolioId;

    int funds;

    String loyaltyLevel;

    List<Holding> holdings;
}
