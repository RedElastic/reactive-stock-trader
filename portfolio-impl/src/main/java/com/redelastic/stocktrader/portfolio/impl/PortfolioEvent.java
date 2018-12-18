package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.serialization.Jsonable;

public interface PortfolioEvent extends Jsonable, AggregateEvent<PortfolioEvent> {


}
