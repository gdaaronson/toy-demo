package com.toydemo.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionConversionResponse(
        @JsonProperty("uniqueIdentifier") String uniqueIdentifier,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate transactionDate,
        @JsonProperty("purchaseAmount") BigDecimal purchaseAmount,
        @JsonProperty("exchangeRate") BigDecimal exchangeRate,
        @JsonProperty("convertedAmount") BigDecimal convertedAmount
) {
}
