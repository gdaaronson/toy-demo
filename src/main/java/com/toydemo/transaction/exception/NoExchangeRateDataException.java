package com.toydemo.transaction.exception;

public class NoExchangeRateDataException extends RuntimeException {

    public NoExchangeRateDataException() {
        super("no exchange rate data exists within a 6 month window");
    }
}
