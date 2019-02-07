package com.redelastic.stocktrader;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class TransferId {

    public static PathParamSerializer<TransferId> pathParamSerializer =
            PathParamSerializers.required("TransferId", TransferId::new, TransferId::getId);
    @NonNull String id;

    public static TransferId newId() { return new TransferId(UUID.randomUUID().toString()); }
}
