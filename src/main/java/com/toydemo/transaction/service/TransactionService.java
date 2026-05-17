package com.toydemo.transaction.service;

import com.toydemo.transaction.TransactionUtils;
import com.toydemo.transaction.Transaction;
import com.toydemo.transaction.TransactionRepository;
import com.toydemo.transaction.client.TreasuryExchangeRateClient;
import com.toydemo.transaction.client.TreasuryExchangeRateRecord;
import com.toydemo.transaction.dto.CreateTransactionRequest;
import com.toydemo.transaction.dto.TransactionConversionResponse;
import com.toydemo.transaction.dto.TransactionResponse;
import com.toydemo.transaction.exception.DuplicateUniqueIdentifierException;
import com.toydemo.transaction.exception.NoExchangeRateDataException;
import com.toydemo.transaction.exception.TransactionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final TreasuryExchangeRateClient treasuryClient;

    public TransactionService(TransactionRepository repository, TreasuryExchangeRateClient treasuryClient) {
        this.repository = repository;
        this.treasuryClient = treasuryClient;
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
                TransactionUtils.roundToCents(request.getPurchaseAmount())
        );

        return TransactionResponse.from(repository.save(transaction));
    }

    public TransactionConversionResponse getConversion(String uniqueIdentifier, String currency) {
        Transaction transaction = repository.findByUniqueIdentifier(uniqueIdentifier)
                .orElseThrow(() -> new TransactionNotFoundException(uniqueIdentifier));

        LocalDate transactionDate = transaction.getTransactionDate();
        LocalDate startWindow = transactionDate.minusMonths(6);

        List<TreasuryExchangeRateRecord> ratesResponse = treasuryClient.fetchRates(currency, startWindow);
        if (ratesResponse.isEmpty()) {
            throw new NoExchangeRateDataException(currency);
        }

        TreasuryExchangeRateRecord closest =
                TransactionUtils.findClosestExchangeRateToTransactionDate(transactionDate, startWindow, ratesResponse);

        if (closest == null) {
            throw new NoExchangeRateDataException(startWindow, transactionDate, currency);
        }

        BigDecimal exchangeRate = TransactionUtils.parseExchangeRate(closest.exchangeRate());
        BigDecimal convertedAmount = TransactionUtils.roundToCents(
                transaction.getPurchaseAmount().multiply(exchangeRate)
        );

        return new TransactionConversionResponse(
                transaction.getUniqueIdentifier(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getPurchaseAmount(),
                exchangeRate,
                convertedAmount
        );
    }
}
