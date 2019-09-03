package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.TransferId;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(TransferEvent.TransferInitiated.class),
        @JsonSubTypes.Type(TransferEvent.FundsRetrieved.class),
        @JsonSubTypes.Type(TransferEvent.CouldNotSecureFunds.class),
        @JsonSubTypes.Type(TransferEvent.DeliveryConfirmed.class),
        @JsonSubTypes.Type(TransferEvent.DeliveryFailed.class),
        @JsonSubTypes.Type(TransferEvent.RefundDelivered.class)
})
public abstract class TransferEvent implements AggregateEvent<TransferEvent>, Jsonable {

    static final int NUM_SHARDS = 4;
    public static AggregateEventShards<TransferEvent> TAG = AggregateEventTag.sharded(TransferEvent.class, NUM_SHARDS);
    private TransferEvent() {}

    @Override
    public AggregateEventTagger<TransferEvent> aggregateTag() {
        return TAG;
    }

    public abstract <T> T visit(Visitor<T> visitor);

    public interface Visitor<T> {
        T visit(TransferInitiated transferInitiated);

        T visit(FundsRetrieved fundsRetrieved);

        T visit(CouldNotSecureFunds couldNotSecureFunds);

        T visit(DeliveryConfirmed deliveryConfirmed);

        T visit(DeliveryFailed deliveryFailed);

        T visit(RefundDelivered refundDelivered);
    }

    public abstract TransferId getTransferId();
    public abstract TransferDetails getTransferDetails();

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class TransferInitiated extends TransferEvent {
        @NonNull public TransferId transferId;
        @NonNull public TransferDetails transferDetails;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class FundsRetrieved extends TransferEvent {
        @NonNull public TransferId transferId;
        @NonNull public TransferDetails transferDetails;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class CouldNotSecureFunds extends TransferEvent {
        @NonNull public TransferId transferId;
        @NonNull public TransferDetails transferDetails;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DeliveryConfirmed extends TransferEvent {
        @NonNull public TransferId transferId;
        @NonNull public TransferDetails transferDetails;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DeliveryFailed extends TransferEvent {
        @NonNull public TransferId transferId;
        @NonNull public TransferDetails transferDetails;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class RefundDelivered extends TransferEvent {
        @NonNull public TransferId transferId;
        @NonNull public TransferDetails transferDetails;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }
}
