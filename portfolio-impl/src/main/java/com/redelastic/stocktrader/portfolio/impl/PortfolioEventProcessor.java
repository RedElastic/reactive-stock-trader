/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.redelastic.stocktrader.portfolio.impl;

import java.util.List;
import java.util.concurrent.CompletionStage;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.Done;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import com.redelastic.stocktrader.portfolio.impl.PortfolioEvent.Opened;
import com.redelastic.stocktrader.portfolio.impl.PortfolioEvent.Closed;

import javax.inject.Inject;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatement;

public class PortfolioEventProcessor extends ReadSideProcessor<PortfolioEvent> {

  private final CassandraSession session;
  private final CassandraReadSide readSide;

  private PreparedStatement writePortfolios = null; // initialized in prepare

  @Inject
  public PortfolioEventProcessor(CassandraSession session, CassandraReadSide readSide) {
    this.session = session;
    this.readSide = readSide;
  }

  private void setWritePortfolios(PreparedStatement writePortfolios) {
    this.writePortfolios = writePortfolios;
  }

  @Override
  public PSequence<AggregateEventTag<PortfolioEvent>> aggregateTags() {
    return TreePVector.singleton(PortfolioEventTag.INSTANCE);
  }

  @Override
  public ReadSideHandler<PortfolioEvent> buildHandler() {
    return readSide.<PortfolioEvent>builder("portfolio_offset")
            .setGlobalPrepare(this::prepareCreateTables)
            .setPrepare((ignored) -> prepareWritePortfolios())
            .setEventHandler(Opened.class, this::processPortfolioChanged)
            .build();
  }

  private CompletionStage<Done> prepareCreateTables() {
    // @formatter:off
    return session.executeCreateTable(
        "CREATE TABLE IF NOT EXISTS portfolio ("
          + "portfolioId text, name text, "
          + "PRIMARY KEY (portfolioId))");
    // @formatter:on
  }

  private CompletionStage<Done> prepareWritePortfolios() {
    return session.prepare("INSERT INTO portfolio (userId, followedBy) VALUES (?, ?)").thenApply(ps -> {
      setWritePortfolios(ps);
      return Done.getInstance();
    });
  }

  private CompletionStage<List<BoundStatement>> processPortfolioChanged(Opened event) {
    BoundStatement bindWritePortfolios = writePortfolios.bind();
    bindWritePortfolios.setString("portfolioId", String.valueOf(event.portfolioId));
    bindWritePortfolios.setString("name", event.name);
    return completedStatement(bindWritePortfolios);
  }

}