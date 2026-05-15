package com.toydemo.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.toydemo.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        String uniqueIdentifier,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate transactionDate,
        BigDecimal purchaseAmount
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getUniqueIdentifier(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getPurchaseAmount()
        );
    }
}
