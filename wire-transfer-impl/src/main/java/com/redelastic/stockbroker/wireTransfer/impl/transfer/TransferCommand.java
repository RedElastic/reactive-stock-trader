package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.redelastic.stocktrader.wiretransfer.api.Account;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;

public abstract class TransferCommand {
    private TransferCommand() {}

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    public static class TransferFunds extends TransferCommand implements ReplyType<Done> {
        Account source;
        Account destination;
        BigDecimal amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class RequestFundsSucessful extends TransferCommand implements ReplyType<Done> {
        private RequestFundsSucessful() {}
        public static RequestFundsSucessful INSTANCE = new RequestFundsSucessful();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class RequestFundsFailed extends TransferCommand implements ReplyType<Done> {
        private RequestFundsFailed() {}
        public static RequestFundsFailed INSTANCE = new RequestFundsFailed();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class SendFunds extends TransferCommand implements ReplyType<Done> {
        private SendFunds() {}
        public static SendFunds INSTANCE = new SendFunds();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DeliveryFailed extends TransferCommand implements ReplyType<Done> {
        private DeliveryFailed() {}
        public static DeliveryFailed INSTANCE = new DeliveryFailed();
    }


    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DeliverySuccessful extends TransferCommand implements ReplyType<Done> {
        private DeliverySuccessful() {}
        public static DeliverySuccessful INSTANCE = new DeliverySuccessful();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class SendRefund extends TransferCommand implements ReplyType<Done> {
        private SendRefund() {}
        public static SendRefund INSTANCE = new SendRefund();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class RefundSuccess extends TransferCommand implements ReplyType<Done> {
        private RefundSuccess() {}
        public static RefundSuccess INSTANCE = new RefundSuccess();
    }
}
