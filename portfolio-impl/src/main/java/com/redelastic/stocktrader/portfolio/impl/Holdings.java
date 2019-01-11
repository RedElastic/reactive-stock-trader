package com.redelastic.stocktrader.portfolio.impl;

import lombok.NonNull;
import lombok.Value;
import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PSequence;

import static java.util.stream.Collectors.toList;


// TODO: preserve stable ordering? Maybe, or just order by symbol.

@Value
class Holdings {

    @NonNull PMap<String, Integer> holdings;

    static Holdings EMPTY = new Holdings(HashTreePMap.empty());

    Holdings add(String symbol, int newShares) {
        int currentShares = 0;
        if (holdings.containsKey(symbol)) {
            currentShares = holdings.get(symbol);
        }
        return new Holdings(holdings.plus(symbol, currentShares + newShares));
    }

    Holdings remove(String symbol, int sharesToRemove) {
        if (sharesToRemove <= 0) {
            throw new IllegalArgumentException("Number of shares to remove from Holdings must be positive.");
        }
        if (holdings.containsKey(symbol)) {
            int currentShares = holdings.get(symbol);
            int remainingShares = currentShares - sharesToRemove;
            if (remainingShares > 0) {
                return new Holdings(holdings.plus(symbol, remainingShares));
            } else if (remainingShares == 0) {
                return new Holdings(holdings.minus(symbol));
            } else {
                throw new IllegalStateException("Attempt to remove more shares from Holdings than are currently available.");
            }
        } else {
            throw new IllegalStateException(
                    String.format("Attempt to remove shares for symbol %s not contained in Holdings.", symbol));
        }
    }

    PSequence<Holding> asSequence() {
         return ConsPStack.from(
                 holdings.keySet().stream()
                .map(symbol -> new Holding(symbol, holdings.get(symbol)))
                .collect(toList()));
    }

    int getShareCount(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }
}
