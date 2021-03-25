package com.redelastic.stocktrader.broker.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@JsonDeserialize(builder = Quote.QuoteBuilder.class)
@Builder
@Data
@Getter
public class Quote {
    String symbol;
    String companyName;
    String primaryExchange;
    String calculationPrice;
    String open;
    String openTime;
    String openSource;
    String close;
    String closeTime;
    String closeSource;
    String high;
    String highTime;
    String highSource;
    String low;
    String lowTime;
    String lowSource;
    String latestPrice;
    String latestSource;
    String latestTime;
    String latestUpdate;
    String latestVolume;
    String iexRealtimePrice;
    String iexRealtimeSize;
    String iexLastUpdated;
    String delayedPrice;
    String delayedPriceTime;
    String oddLotDelayedPrice;
    String oddLotDelayedPriceTime;
    String extendedPrice;
    String extendedChange;
    String extendedChangePercent;
    String extendedPriceTime;
    String previousClose;
    String previousVolume;
    String change;
    String changePercent;
    String volume;
    String iexMarketPercent;
    String iexVolume;
    String avgTotalVolume;
    String iexBidPrice;
    String iexBidSize;
    String iexAskPrice;
    String iexAskSize;
    String iexOpen;
    String iexOpenTime;
    String iexClose;
    String iexCloseTime;
    String marketCap;
    String peRatio;
    String week52High;
    String week52Low;
    String ytdChange;
    String lastTradeTime;
    String isUSMarketOpen;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class QuoteBuilder {
    }
}