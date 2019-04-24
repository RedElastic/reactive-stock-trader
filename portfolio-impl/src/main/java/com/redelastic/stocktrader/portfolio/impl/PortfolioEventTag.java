package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class PortfolioEventTag {

  public static final AggregateEventTag<PortfolioEvent> INSTANCE = 
    AggregateEventTag.of(PortfolioEvent.class);

}