/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class OrderId {

    public static PathParamSerializer<OrderId> pathParamSerializer =
            PathParamSerializers.required("OrderId", OrderId::new, OrderId::getId);
    @NonNull String id;

    public static OrderId newId() { return new OrderId(UUID.randomUUID().toString()); }
}
