package com.redelastic.stockbroker.wireTransfer.impl.transfer;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.redelastic.stocktrader.wiretransfer.api.Account;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;

public abstract class TransferCommand {
    private TransferCommand() {}

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Start extends TransferCommand implements ReplyType<Done> {
        Account source;
        Account destination;
        BigDecimal amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class ReceiveFunds extends TransferCommand implements ReplyType<Done> {
        private ReceiveFunds() {}
        static ReceiveFunds INSTANCE = new ReceiveFunds();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class SendFunds extends TransferCommand implements ReplyType<Done> {
        private SendFunds() {}
        static SendFunds INSTANCE = new SendFunds();
    }
}
