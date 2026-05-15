package com.toydemo.transaction.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String uniqueIdentifier) {
        super("transaction not found: " + uniqueIdentifier);
    }
}
