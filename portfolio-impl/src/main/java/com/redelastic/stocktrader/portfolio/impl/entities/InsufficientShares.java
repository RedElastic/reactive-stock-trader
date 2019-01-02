package com.redelastic.stocktrader.portfolio.impl.entities;

import com.lightbend.lagom.javadsl.api.transport.TransportErrorCode;
import com.lightbend.lagom.javadsl.api.transport.TransportException;

public class InsufficientShares extends TransportException {
    private static final long serialVersionUID = 1L;

    public InsufficientShares(String message) {
        super(TransportErrorCode.BadRequest, message);
    }
}
