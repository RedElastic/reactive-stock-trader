package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import com.redelastic.stocktrader.TransferId;
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
    public static class TransferInitiated extends TransferEvent {
        @NonNull TransferId transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class FundsRetrieved extends TransferEvent {
        @NonNull TransferId transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class CouldNotSecureFunds extends TransferEvent {
        @NonNull TransferId transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class FundsSent extends TransferEvent {
        @NonNull TransferId transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DeliveryConfirm extends TransferEvent {
        @NonNull TransferId transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DeliveryFailed extends TransferEvent {
        @NonNull TransferId transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class RefundSent extends TransferEvent {
        @NonNull TransferId transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class RefundDelivered extends TransferEvent {
        @NonNull TransferId transferId;
        @NonNull Account source;
        @NonNull Account destination;
        @NonNull BigDecimal amount;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }


    public interface Visitor<T> {
        T visit(TransferInitiated transferInitiated);
        T visit(FundsRetrieved fundsRetrieved);
        T visit(CouldNotSecureFunds couldNotSecureFunds);
        T visit(FundsSent fundsSent);
        T visit(DeliveryConfirm deliveryConfirm);
        T visit(DeliveryFailed deliveryFailed);
        T visit(RefundSent refundSent);
        T visit(RefundDelivered refundDelivered);
    }

    public abstract <T> T visit(Visitor<T> visitor);
}
