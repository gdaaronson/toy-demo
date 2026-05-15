package com.toydemo.transaction.exception;

public class DuplicateUniqueIdentifierException extends RuntimeException {

    public DuplicateUniqueIdentifierException(String uniqueIdentifier) {
        super("unique identifier already exists: " + uniqueIdentifier);
    }
}
