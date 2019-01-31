package com.redelastic.stocktrader;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class OrderId {

    @NonNull String id;

    public static OrderId newId() { return new OrderId(UUID.randomUUID().toString()); }

    public static PathParamSerializer<OrderId> pathParamSerializer =
            PathParamSerializers.required("OrderId", OrderId::new, OrderId::getId);
}
