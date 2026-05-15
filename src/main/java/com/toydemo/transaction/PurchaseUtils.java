package com.toydemo.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PurchaseUtils {

    private PurchaseUtils() {
    }

    public static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
