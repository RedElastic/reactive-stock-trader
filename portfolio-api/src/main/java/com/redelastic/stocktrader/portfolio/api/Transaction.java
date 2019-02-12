package com.redelastic.stocktrader.portfolio.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.redelastic.stocktrader.OrderId;
import com.redelastic.stocktrader.TransferId;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(Transaction.SharesSold.class),
        @JsonSubTypes.Type(Transaction.SharesBought.class),
        @JsonSubTypes.Type(Transaction.TransferSent.class),
        @JsonSubTypes.Type(Transaction.TransferReceived.class)
})
public abstract class Transaction {
    private Transaction() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class SharesSold extends Transaction {
        @NonNull OrderId orderId;
        @NonNull String symbol;
        int shares;
        @NonNull BigDecimal sharePrice;
        @NonNull BigDecimal fundsBalance;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class SharesBought extends Transaction {
        @NonNull OrderId orderId;
        @NonNull String symbol;
        int shares;
        @NonNull BigDecimal sharePrice;
        @NonNull BigDecimal fundsBalance;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class TransferSent extends Transaction {
        @NonNull TransferId transferId;
        @NonNull BigDecimal amount;
        @NonNull BigDecimal fundsBalance;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class TransferReceived extends Transaction {
        @NonNull TransferId transferId;
        @NonNull BigDecimal amount;
        @NonNull BigDecimal fundsBalance;
    }
}
