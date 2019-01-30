package com.redelastic.stocktrader;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.redelastic.stocktrader.order.OrderId;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import java.util.UUID;

@Value
public class PortfolioId {
    @NonNull String id;

    public static PortfolioId newId() { return new PortfolioId(UUID.randomUUID().toString()); }

    public static PathParamSerializer<PortfolioId> pathParamSerializer = new PathParamSerializer<PortfolioId>() {
        @Override
        public PSequence<String> serialize(PortfolioId parameter) {
            return ConsPStack.singleton(parameter.getId());
        }

        @Override
        public PortfolioId deserialize(PSequence<String> parameters) {
            return new PortfolioId(parameters.get(0)); // FIXME: how do we handle errors?
        }
    };

}
