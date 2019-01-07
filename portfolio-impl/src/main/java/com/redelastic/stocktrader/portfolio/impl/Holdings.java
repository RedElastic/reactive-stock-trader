package com.redelastic.stocktrader.portfolio.impl;

import lombok.Value;
import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PSequence;

import static java.util.stream.Collectors.toList;


// TODO: preserve stable ordering

@Value
public class Holdings {

    PMap<String, Integer> holdings;

    public Holdings add(String symbol, int newShares) {
        int currentShares = 0;
        if (holdings.containsKey(symbol)) {
            currentShares = holdings.get(symbol);
        }
        return new Holdings(holdings.plus(symbol, currentShares + newShares));
    }

    public static Holdings EMPTY = new Holdings(HashTreePMap.empty());

    public PSequence<Holding> asSequence() {
         return ConsPStack.from(
                 holdings.keySet().stream()
                .map(symbol -> new Holding(symbol, holdings.get(symbol)))
                .collect(toList()));
    }

    public int getShareCount(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }
}
