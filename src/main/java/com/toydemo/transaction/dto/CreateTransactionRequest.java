package com.toydemo.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.toydemo.transaction.validation.AmountInCents;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateTransactionRequest {

    @NotBlank(message = "description is required")
    @Size(max = 50, message = "description must be at most 50 characters")
    private String description;

    @NotNull(message = "transaction date is required")
    @PastOrPresent(message = "transaction date cannot be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("transactionDate")
    private LocalDate transactionDate;

    @NotNull(message = "purchase amount is required")
    @AmountInCents
    @JsonProperty("purchaseAmount")
    private BigDecimal purchaseAmount;

    @NotBlank(message = "unique identifier is required")
    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(BigDecimal purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }
}
