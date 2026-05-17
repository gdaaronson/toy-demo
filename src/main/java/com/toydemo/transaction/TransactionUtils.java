package com.toydemo.transaction;

import com.toydemo.transaction.client.TreasuryExchangeRateRecord;
import com.toydemo.transaction.exception.NoExchangeRateDataException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

public final class TransactionUtils {

    private TransactionUtils() {
    }

    public static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static TreasuryExchangeRateRecord findClosestExchangeRateToTransactionDate(
            LocalDate transactionDate,
            List<TreasuryExchangeRateRecord> rates
    ) {
        LocalDate windowStart = transactionDate.minusMonths(6);

        return rates.stream()
            .filter(rate -> {
                java.time.LocalDate rd = rate.recordDate();
                return !rd.isBefore(windowStart) && !rd.isAfter(transactionDate);
            })
            .min(Comparator.comparingLong(rate ->
                Math.abs(ChronoUnit.DAYS.between(transactionDate, rate.recordDate()))))
            .orElse(null);
    }

    public static BigDecimal parseExchangeRate(String exchangeRate) {
        try {
            return new BigDecimal(exchangeRate);
        } catch (NumberFormatException ex) {
            throw new NoExchangeRateDataException("invalid exchange rate format", ex);
        }
    }
}
