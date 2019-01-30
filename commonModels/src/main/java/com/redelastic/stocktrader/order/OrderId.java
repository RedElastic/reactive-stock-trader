package com.redelastic.stocktrader.order;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.redelastic.stocktrader.PortfolioId;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import java.util.UUID;

@Value
public class OrderId {
    @NonNull String id;

    public static OrderId newId() { return new OrderId(UUID.randomUUID().toString()); }

    public static PathParamSerializer<OrderId> pathParamSerializer = new PathParamSerializer<OrderId>() {
        @Override
        public PSequence<String> serialize(OrderId parameter) {
            return ConsPStack.singleton(parameter.getId());
        }

        @Override
        public OrderId deserialize(PSequence<String> parameters) {
            return new OrderId(parameters.get(0)); // FIXME: how do we handle errors?
        }
    };
}
