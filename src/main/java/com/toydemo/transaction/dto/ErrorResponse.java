package com.toydemo.transaction.dto;

import java.util.List;

public record ErrorResponse(String message, List<FieldError> errors) {

    public record FieldError(String field, String message) {
    }
}
