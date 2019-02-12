/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

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
    public static class RequestFundsSuccessful extends TransferCommand implements ReplyType<Done> {
        public static RequestFundsSuccessful INSTANCE = new RequestFundsSuccessful();

        private RequestFundsSuccessful() {}
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class RequestFundsFailed extends TransferCommand implements ReplyType<Done> {
        public static RequestFundsFailed INSTANCE = new RequestFundsFailed();

        private RequestFundsFailed() {}
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DeliveryFailed extends TransferCommand implements ReplyType<Done> {
        public static DeliveryFailed INSTANCE = new DeliveryFailed();

        private DeliveryFailed() {}
    }


    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class DeliverySuccessful extends TransferCommand implements ReplyType<Done> {
        public static DeliverySuccessful INSTANCE = new DeliverySuccessful();

        private DeliverySuccessful() {}
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class RefundSuccessful extends TransferCommand implements ReplyType<Done> {
        public static RefundSuccessful INSTANCE = new RefundSuccessful();

        private RefundSuccessful() {}
    }
}
