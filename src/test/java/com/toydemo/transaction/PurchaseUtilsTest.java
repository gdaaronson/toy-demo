package com.toydemo.transaction;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseUtilsTest {

    @Test
    void roundsHalfUpAtHalfCent() {
        assertEquals(new BigDecimal("10.01"), PurchaseUtils.roundToCents(new BigDecimal("10.005")));
    }

    @Test
    void roundsDownBelowHalfCent() {
        assertEquals(new BigDecimal("10.00"), PurchaseUtils.roundToCents(new BigDecimal("10.004")));
    }
}
