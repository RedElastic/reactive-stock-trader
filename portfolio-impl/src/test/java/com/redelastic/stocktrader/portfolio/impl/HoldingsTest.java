/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

package com.redelastic.stocktrader.portfolio.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HoldingsTest {

    @Test
    public void add() {
        Holdings holdings =
                Holdings.EMPTY
                        .add("ABC", 10);

        assertEquals(holdings.getShareCount("ABC"), 10);

    }

    @Test
    public void remove() {
        Holdings holdings =
                Holdings.EMPTY
                        .add("ABC", 10)
                        .remove("ABC", 8);

        assertEquals(holdings.getShareCount("ABC"), 2);

    }

    @Test
    public void asSequence() {
    }

}