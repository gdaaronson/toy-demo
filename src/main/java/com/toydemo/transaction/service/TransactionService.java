package com.toydemo.transaction.service;

import com.toydemo.transaction.PurchaseUtils;
import com.toydemo.transaction.Transaction;
import com.toydemo.transaction.TransactionRepository;
import com.toydemo.transaction.dto.CreateTransactionRequest;
import com.toydemo.transaction.dto.TransactionResponse;
import com.toydemo.transaction.exception.DuplicateUniqueIdentifierException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        if (repository.existsByUniqueIdentifier(request.getUniqueIdentifier())) {
            throw new DuplicateUniqueIdentifierException(request.getUniqueIdentifier());
        }

        Transaction transaction = new Transaction(
                request.getUniqueIdentifier(),
                request.getDescription(),
                request.getTransactionDate(),
                PurchaseUtils.roundToCents(request.getPurchaseAmount())
        );

        return TransactionResponse.from(repository.save(transaction));
    }
}
