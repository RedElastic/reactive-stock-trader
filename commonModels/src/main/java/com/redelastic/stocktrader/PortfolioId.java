package com.redelastic.stocktrader;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
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

    public static PathParamSerializer<PortfolioId> pathParamSerializer =
            PathParamSerializers.required("PortfolioId", PortfolioId::new, PortfolioId::getId);
}
