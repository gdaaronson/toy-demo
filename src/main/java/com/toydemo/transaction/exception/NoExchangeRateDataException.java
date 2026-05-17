package com.toydemo.transaction.exception;

import java.time.LocalDate;
public class NoExchangeRateDataException extends RuntimeException {

    public NoExchangeRateDataException(LocalDate startWindow, LocalDate transactionDate, String currency) {
        super("no exchange rate data exists between " + startWindow + " and " + transactionDate + " for currency " + currency);
    }

    public NoExchangeRateDataException(String currency) {
        super("no exchange rate data found for currency " + currency);
    }

    public NoExchangeRateDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
