package com.toydemo.transaction.exception;

public class ParameterTooLongException extends RuntimeException {

    private final int length;
    private final int max;
    private final String param;

    public ParameterTooLongException(int length, int max, String param) {
        super(normalizeParam(param) + " is too long: " + length + " characters (max " + max + ")");
        this.length = length;
        this.max = max;
        this.param = param;
    }

    private static String normalizeParam(String param) {
        return switch (param.toLowerCase()) {
            case "description" -> "description";
            case "unique identifier", "uniqueidentifier", "unique_identifier" -> "unique identifier";
            default -> param;
        };
    }

    public int getLength() {
        return length;
    }

    public int getMax() {
        return max;
    }

    public String getParam() {
        return param;
    }

    public String getFieldName() {
        return switch (param.toLowerCase()) {
            case "description" -> "description";
            case "unique identifier", "uniqueidentifier", "unique_identifier" -> "uniqueIdentifier";
            default -> param;
        };
    }
}
