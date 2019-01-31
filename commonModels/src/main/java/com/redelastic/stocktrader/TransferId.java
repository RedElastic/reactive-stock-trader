package com.redelastic.stocktrader;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import java.util.UUID;

@Value
public class TransferId {
    @NonNull String id;

    public static TransferId newId() { return new TransferId(UUID.randomUUID().toString()); }

    public static PathParamSerializer<TransferId> pathParamSerializer =
            PathParamSerializers.required("TransferId", TransferId::new, TransferId::getId);
}
