package com.redelastic.stocktrader;

import com.lightbend.lagom.javadsl.api.deser.PathParamSerializer;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class PortfolioId {

    public static PathParamSerializer<PortfolioId> pathParamSerializer = PathParamSerializers.required("PortfolioId", PortfolioId::new, PortfolioId::getId);
    
    @NonNull String id;

    public PortfolioId() { this.id = ""; }

    public PortfolioId(String id) { this.id = id; }

    public static PortfolioId newId() { return new PortfolioId(UUID.randomUUID().toString()); }

    public String getId() { return id; }
}
