package com.redelastic.stocktrader.wiretransfer.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.pcollections.PSequence;
import java.math.BigDecimal;

@Value
@Builder
public class TransactionSummary {

	public TransactionSummary(String id, String status, String dateTime, String source, String destination, String amount) { 
		this.id = id;
		this.status = status;
		this.dateTime = dateTime;
		this.source = source;
		this.destination = destination;
		this.amount = amount;
	}

    @NonNull public String id;
    @NonNull public String status;
    @NonNull public String dateTime;
    @NonNull public String source;
    @NonNull public String destination;
    @NonNull public String amount;
}
