package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.api.transport.TransportErrorCode;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import com.lightbend.lagom.serialization.Jsonable;

public class InsufficientShares extends TransportException implements Jsonable {

    @Override
    public TransportErrorCode errorCode() {
        return TransportErrorCode.fromHttp(422);
    }

    public InsufficientShares(String message) {
        super(TransportErrorCode.BadRequest, message);
    }
}
