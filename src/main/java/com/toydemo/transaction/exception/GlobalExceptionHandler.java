package com.toydemo.transaction.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.toydemo.transaction.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestParameter(MissingServletRequestParameterException ex) {
        return new ErrorResponse(
                "validation failed",
                List.of(new ErrorResponse.FieldError(ex.getParameterName(), ex.getParameterName() + " is required")));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodValidation(HandlerMethodValidationException ex) {
        List<ErrorResponse.FieldError> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ErrorResponse.FieldError(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage())))
                .toList();
        return new ErrorResponse("validation failed", errors);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTransactionNotFound(TransactionNotFoundException ex) {
        return new ErrorResponse(
                ex.getMessage(),
                List.of(new ErrorResponse.FieldError("uniqueIdentifier", ex.getMessage()))
        );
    }

    @ExceptionHandler(NoExchangeRateDataException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoExchangeRateData(NoExchangeRateDataException ex) {
        return new ErrorResponse(ex.getMessage(), List.of());
    }

    @ExceptionHandler(TreasuryApiUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleTreasuryUnavailable(TreasuryApiUnavailableException ex) {
        return new ErrorResponse(ex.getMessage(), List.of());
    }

    @ExceptionHandler(ParameterTooLongException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleParameterTooLong(ParameterTooLongException ex) {
        return new ErrorResponse("validation failed", List.of(
                new ErrorResponse.FieldError(ex.getFieldName(), ex.getMessage())
        ));
    }

    private ErrorResponse.FieldError toFieldError(FieldError fieldError) {
        return new ErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
