package com.redelastic.stocktrader;

import com.fasterxml.jackson.annotation.JsonValue;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class PortfolioId {

    @NonNull String id;

    public static PortfolioId newId() { return new PortfolioId(UUID.randomUUID().toString()); }

    public static PathParamSerializer<PortfolioId> pathParamSerializer =
            PathParamSerializers.required("PortfolioId", PortfolioId::new, PortfolioId::getId);
}
