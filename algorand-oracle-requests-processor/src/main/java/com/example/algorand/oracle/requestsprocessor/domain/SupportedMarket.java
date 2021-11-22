package com.example.algorand.oracle.requestsprocessor.domain;

import java.util.stream.Stream;

public enum SupportedMarket {
    EUR_USD("EUR/USD"),
    USD_EUR("USD/EUR");
    private final String market;

    SupportedMarket(String market) {
        this.market = market;
    }

    public String getMarket() {
        return market;
    }
    public static SupportedMarket fromMarketName(String market) {
       return Stream.of(SupportedMarket.values())
                .filter(m -> m.market.equalsIgnoreCase(market))
                .findFirst()
                .orElse(null);
    }
}
