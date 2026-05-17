package com.toydemo.transaction.exception;

import java.time.LocalDate;
public class NoExchangeRateDataException extends RuntimeException {

    public NoExchangeRateDataException(LocalDate windowStart, LocalDate transactionDate) {
        super("no exchange rate data exists between " + windowStart + " and " + transactionDate);
    }

    public NoExchangeRateDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
