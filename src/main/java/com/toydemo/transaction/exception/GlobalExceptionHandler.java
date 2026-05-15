package com.toydemo.transaction.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.toydemo.transaction.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return new ErrorResponse("validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormat) {
            String field = invalidFormat.getPath().isEmpty()
                    ? "request"
                    : invalidFormat.getPath().get(0).getFieldName();
            return new ErrorResponse(
                    "validation failed",
                    List.of(new ErrorResponse.FieldError(field, "invalid format"))
            );
        }
        return new ErrorResponse("invalid request body", List.of());
    }

    @ExceptionHandler(DuplicateUniqueIdentifierException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDuplicate(DuplicateUniqueIdentifierException ex) {
        return new ErrorResponse(
                "validation failed",
                List.of(new ErrorResponse.FieldError("uniqueIdentifier", ex.getMessage()))
        );
    }

    private ErrorResponse.FieldError toFieldError(FieldError fieldError) {
        return new ErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
