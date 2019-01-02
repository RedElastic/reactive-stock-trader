package com.redelastic.stocktrader.broker.api;

public abstract class OrderType {
    public static class MarketBuy extends OrderType {
        private MarketBuy() {}
        public static MarketBuy INSTANCE = new MarketBuy();
    }
}
