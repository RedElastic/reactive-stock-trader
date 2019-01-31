package com.redelastic.stocktrader.order;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.redelastic.stocktrader.PortfolioId;
import com.redelastic.stocktrader.TransferId;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.ConsPStack;
import org.pcollections.PSequence;

import java.util.UUID;

@Value
public class OrderId {
    @NonNull String id;

    public static OrderId newId() { return new OrderId(UUID.randomUUID().toString()); }

    public static PathParamSerializer<OrderId> pathParamSerializer =
            PathParamSerializers.required("OrderId", OrderId::new, OrderId::getId);
}
