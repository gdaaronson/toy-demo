package com.toydemo.transaction.exception;

public class TreasuryApiUnavailableException extends RuntimeException {

    public TreasuryApiUnavailableException() {
        super("treasury api unavailable");
    }

    public TreasuryApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
