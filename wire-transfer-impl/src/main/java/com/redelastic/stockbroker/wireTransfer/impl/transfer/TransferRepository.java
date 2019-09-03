package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.redelastic.stocktrader.TransferId;

public interface TransferRepository {
    PersistentEntityRef<TransferCommand> get(TransferId transferId);

    Source<Pair<TransferEvent, Offset>, NotUsed> eventStream(AggregateEventTag<TransferEvent> tag, Offset offset);
}
