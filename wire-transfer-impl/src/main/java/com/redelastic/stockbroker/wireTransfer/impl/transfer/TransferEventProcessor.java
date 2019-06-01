package com.redelastic.stockbroker.wireTransfer.impl.transfer;

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
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferEvent.*;
import com.redelastic.stocktrader.wiretransfer.api.*;

import javax.inject.Inject;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.*;

import java.util.Arrays;
import java.sql.Timestamp;

public class TransferEventProcessor extends ReadSideProcessor<TransferEvent> {

  private final CassandraSession session;
  private final CassandraReadSide readSide;

  private PreparedStatement writeTransfers = null; // initialized in prepare

  @Inject
  public TransferEventProcessor(CassandraSession session, CassandraReadSide readSide) {
    this.session = session;
    this.readSide = readSide;
  }

  @Override
  public PSequence<AggregateEventTag<TransferEvent>> aggregateTags() {
    return TransferEvent.TAG.allTags();
  }

  @Override
  public ReadSideHandler<TransferEvent> buildHandler() {
    return readSide.<TransferEvent>builder("transfer_offset")
      .setGlobalPrepare(this::prepareCreateTables)
      .setPrepare(tag -> prepareWriteTransfers())
      .setEventHandler(TransferInitiated.class, this::processTransferInitiated)
      .setEventHandler(FundsRetrieved.class, this::processFundsRetrieved)
      .setEventHandler(CouldNotSecureFunds.class, this::processCouldNotSecureFunds)
      .setEventHandler(DeliveryConfirmed.class, this::processDeliveryConfirmed)
      .setEventHandler(DeliveryFailed.class, this::processDeliveryFailed)
      .setEventHandler(RefundDelivered.class, this::processRefundDelivered)
      .build();
  }

  private CompletionStage<Done> prepareCreateTables() {
    // @formatter:off
    return session.executeCreateTable(
        "CREATE TABLE IF NOT EXISTS transfer_summary ("
          + "transferId text, "
          + "status text, "
          + "dateTime text, "
          + "source text, "
          + "destination text, "
          + "amount text, "
          + "PRIMARY KEY (transferId))");
    // @formatter:on
  }

  private CompletionStage<Done> prepareWriteTransfers() {
    return session.prepare("INSERT INTO transfer_summary (transferId, status, dateTime, source, destination, amount) VALUES (?, ?, ?, ?, ?, ?)").thenApply(
      ps -> {
        this.writeTransfers = ps;
        return Done.getInstance();
      }
    );
  }

  private CompletionStage<List<BoundStatement>> processTransferInitiated(TransferInitiated event) {
    return processTransferEvent("Transfer Initiated", event);
  }

  private CompletionStage<List<BoundStatement>> processFundsRetrieved(FundsRetrieved event) {
    return processTransferEvent("Funds Retrieved", event);
  }

  private CompletionStage<List<BoundStatement>> processCouldNotSecureFunds(CouldNotSecureFunds event) {
    return processTransferEvent("Could Not Secure Funds", event);
  }

  private CompletionStage<List<BoundStatement>> processDeliveryConfirmed(DeliveryConfirmed event) {
    return processTransferEvent("Delivery Confirmed", event);
  }

  private CompletionStage<List<BoundStatement>> processDeliveryFailed(DeliveryFailed event) {
    return processTransferEvent("Delivery Failed", event);
  }

  private CompletionStage<List<BoundStatement>> processRefundDelivered(RefundDelivered event) {
    return processTransferEvent("Refund Delivered", event);
  }

  private CompletionStage<List<BoundStatement>> processTransferEvent(String status, TransferEvent event) {
    String transferId = event.getTransferId().getId();
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String source = null;
    String destination = null;
    String amount = event.getTransferDetails().getAmount().toString();

    if (event.getTransferDetails().getSource() instanceof Account.Portfolio) {
      source = ((Account.Portfolio) event.getTransferDetails().getSource()).getPortfolioId().getId();  
    } else {
      source = "Savings";
    }

    if (event.getTransferDetails().getDestination() instanceof Account.Portfolio) {
      destination = ((Account.Portfolio) event.getTransferDetails().getDestination()).getPortfolioId().getId();  
    } else {
      destination = "Savings";
    }

    BoundStatement bindWriteTransfers = writeTransfers.bind();
    bindWriteTransfers.setString("transferId", transferId);
    bindWriteTransfers.setString("status", status);
    bindWriteTransfers.setString("dateTime", timestamp.toString());
    bindWriteTransfers.setString("source", source);
    bindWriteTransfers.setString("destination", destination);
    bindWriteTransfers.setString("amount", amount);
    return completedStatements(Arrays.asList(bindWriteTransfers));
  }

}