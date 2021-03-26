package com.redelastic.stocktrader.broker.api;

import java.math.BigDecimal;
import java.math.BigInteger;

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
    BigDecimal open;
    String openTime;
    String openSource;
    BigDecimal close;
    String closeTime;
    String closeSource;
    BigDecimal high;
    String highTime;
    String highSource;
    BigDecimal low;
    String lowTime;
    String lowSource;
    BigDecimal latestPrice;
    String latestSource;
    String latestTime;
    BigInteger latestUpdate;
    BigInteger latestVolume;
    BigDecimal iexRealtimePrice;
    BigInteger iexRealtimeSize;
    BigInteger iexLastUpdated;
    BigDecimal delayedPrice;
    String delayedPriceTime;
    BigDecimal oddLotDelayedPrice;
    String oddLotDelayedPriceTime;
    BigDecimal extendedPrice;
    BigDecimal extendedChange;
    BigDecimal extendedChangePercent;
    String extendedPriceTime;
    BigDecimal previousClose;
    BigInteger previousVolume;
    BigDecimal change;
    BigDecimal changePercent;
    BigInteger volume;
    BigDecimal iexMarketPercent;
    BigInteger iexVolume;
    BigInteger avgTotalVolume;
    BigDecimal iexBidPrice;
    BigInteger iexBidSize;
    BigDecimal iexAskPrice;
    BigInteger iexAskSize;
    BigDecimal iexOpen;
    String iexOpenTime;
    BigDecimal iexClose;
    String iexCloseTime;
    BigInteger marketCap;
    BigDecimal peRatio;
    BigDecimal week52High;
    BigDecimal week52Low;
    BigDecimal ytdChange;
    String lastTradeTime;
    Boolean isUSMarketOpen;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class QuoteBuilder {
    }
}