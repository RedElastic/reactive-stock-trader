package com.redelastic.stocktrader;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import java.util.UUID;

@Value
public class TransferId {
    @NonNull String id;

    public static TransferId newId() { return new TransferId(UUID.randomUUID().toString()); }

    public static PathParamSerializer<TransferId> pathParamSerializer = new PathParamSerializer<TransferId>() {
        @Override
        public PSequence<String> serialize(TransferId parameter) {
            return ConsPStack.singleton(parameter.getId());
        }

        @Override
        public TransferId deserialize(PSequence<String> parameters) {
            return new TransferId(parameters.get(0)); // FIXME: how do we handle errors?
        }
    };
}
