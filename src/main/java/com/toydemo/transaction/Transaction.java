package com.toydemo.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_identifier", nullable = false, unique = true, length = 100)
    private String uniqueIdentifier;

    @Column(nullable = false, length = 50)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "purchase_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchaseAmount;

    protected Transaction() {
    }

    public Transaction(String uniqueIdentifier, String description, LocalDate transactionDate, BigDecimal purchaseAmount) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.description = description;
        this.transactionDate = transactionDate;
        this.purchaseAmount = purchaseAmount;
    }

    public Long getId() {
        return id;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getPurchaseAmount() {
        return purchaseAmount;
    }
}
