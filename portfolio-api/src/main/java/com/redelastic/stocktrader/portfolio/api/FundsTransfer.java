package com.redelastic.stocktrader.portfolio.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.redelastic.stocktrader.TransferId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(FundsTransfer.Deposit.class),
        @JsonSubTypes.Type(FundsTransfer.Withdrawl.class),
        @JsonSubTypes.Type(FundsTransfer.Refund.class)
})
public abstract class FundsTransfer {

    private FundsTransfer() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    @Builder
    public static class Deposit extends FundsTransfer {
        @NonNull TransferId transferId;
        @NonNull BigDecimal funds;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @Builder
    public static class Withdrawl extends FundsTransfer {
        @NonNull TransferId transferId;
        @NonNull BigDecimal funds;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @Builder
    public static class Refund extends FundsTransfer {
        @NonNull TransferId transferId;
        @NonNull BigDecimal funds;

        @Override
        public <T> T visit(Visitor<T> visitor) { return visitor.visit(this); }
    }

    public interface Visitor<T> {
        T visit(Deposit deposit);
        T visit(Withdrawl withdrawl);
        T visit(Refund refund);
    }

    public abstract <T> T visit(Visitor<T> visitor);

}
