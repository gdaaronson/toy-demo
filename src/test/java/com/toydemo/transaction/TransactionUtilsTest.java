package com.toydemo.transaction;

import com.toydemo.transaction.client.TreasuryExchangeRateRecord;
import org.junit.jupiter.api.Test;
import com.toydemo.transaction.exception.NoExchangeRateDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionUtilsTest {

    @Test
    void roundToCents_halfUpAtHalfCent() {
        assertEquals(new BigDecimal("10.01"), TransactionUtils.roundToCents(new BigDecimal("10.005")));
    }

    @Test
    void roundToCents_roundsDownBelowHalfCent() {
        assertEquals(new BigDecimal("10.00"), TransactionUtils.roundToCents(new BigDecimal("10.004")));
    }

    @Test
    void findsClosestExchangeRateToTransactionDate() {
        LocalDate transactionDate = LocalDate.of(2025, 8, 15);
        List<TreasuryExchangeRateRecord> rates = List.of(
                new TreasuryExchangeRateRecord("Canada-Dollar", "1.350", LocalDate.of(2025, 3, 31)),
                new TreasuryExchangeRateRecord("Canada-Dollar", "1.400", LocalDate.of(2025, 9, 30)),
                new TreasuryExchangeRateRecord("Canada-Dollar", "1.300", LocalDate.of(2024, 12, 31))
        );

        TreasuryExchangeRateRecord closest =
                TransactionUtils.findClosestExchangeRateToTransactionDate(transactionDate, rates);

        assertEquals(LocalDate.of(2025, 9, 30), closest.recordDate());
    }

    @Test
    void findClosest_returnsNullWhenNoRatesInWindow() {
        LocalDate transactionDate = LocalDate.of(2025, 1, 15);
        List<TreasuryExchangeRateRecord> rates = List.of(
                new TreasuryExchangeRateRecord("Canada-Dollar", "1.350", LocalDate.of(2024, 3, 31))
        );

        assertNull(TransactionUtils.findClosestExchangeRateToTransactionDate(transactionDate, rates));
    }

    @Test
    void parseExchangeRate_invalidString_throwsNoExchangeRateDataException() {
        String invalid = "not-a-number";
        assertThrows(NoExchangeRateDataException.class, () -> TransactionUtils.parseExchangeRate(invalid));
    }
}
