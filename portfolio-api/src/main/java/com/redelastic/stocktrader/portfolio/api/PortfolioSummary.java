/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.portfolio.api;

import com.redelastic.stocktrader.PortfolioId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.PSequence;

import java.math.BigDecimal;

@Value
@Builder
public class PortfolioSummary {

	public PortfolioSummary(PortfolioId id, String name) { 
		this.portfolioId = id;
		this.name = name;
	}

    @NonNull PortfolioId portfolioId;

    @NonNull String name;
}
