/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.PSequence;

import java.math.BigDecimal;

@Value
@Builder
public class PortfolioSummary {
    @NonNull String portfolioId;

    @NonNull String name;

    @NonNull
    BigDecimal funds;

    PSequence<EquityHolding> equities;

    PSequence<CompletedOrder> completedOrders;
}
