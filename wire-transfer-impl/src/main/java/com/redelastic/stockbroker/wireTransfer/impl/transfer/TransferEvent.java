package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.wiretransfer.api.Account;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

public abstract class TransferEvent implements AggregateEvent<TransferEvent>, Jsonable {

    private TransferEvent() {}

    static final int NUM_SHARDS = 4;
    public static AggregateEventShards<TransferEvent> TAG = AggregateEventTag.sharded(TransferEvent.class, NUM_SHARDS);

    @Override
    public AggregateEventTagger<TransferEvent> aggregateTag() {
        return TAG;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Started extends TransferEvent {
        @NonNull String transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class FundsReceived extends TransferEvent {
        @NonNull String transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class FundsSent extends TransferEvent {
        @NonNull String transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;
    }
}
