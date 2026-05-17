package com.toydemo.transaction.exception;

public class DescriptionTooLongException extends RuntimeException {

    private final int length;
    private final int max;

    public DescriptionTooLongException(int length, int max) {
        super("description is too long: " + length + " characters (max " + max + ")");
        this.length = length;
        this.max = max;
    }

    public int getLength() {
        return length;
    }

    public int getMax() {
        return max;
    }
}
