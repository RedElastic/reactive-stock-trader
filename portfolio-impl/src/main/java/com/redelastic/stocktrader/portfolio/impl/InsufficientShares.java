package com.redelastic.stocktrader.portfolio.impl;

import com.lightbend.lagom.javadsl.api.transport.TransportErrorCode;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import com.lightbend.lagom.serialization.Jsonable;

class InsufficientShares extends TransportException implements Jsonable {

    public InsufficientShares(String message) {
        super(TransportErrorCode.BadRequest, message);
    }
}
