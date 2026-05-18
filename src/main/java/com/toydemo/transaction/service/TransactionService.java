package com.toydemo.transaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toydemo.transaction.Transaction;
import com.toydemo.transaction.TransactionRepository;
import com.toydemo.transaction.TransactionUtils;
import com.toydemo.transaction.audit.ApiDirection;
import com.toydemo.transaction.audit.ApiRequestLog;
import com.toydemo.transaction.audit.ApiRequestLogRepository;
import com.toydemo.transaction.client.TreasuryExchangeRateClient;
import com.toydemo.transaction.client.TreasuryExchangeRateRecord;
import com.toydemo.transaction.dto.CreateTransactionRequest;
import com.toydemo.transaction.dto.TransactionConversionResponse;
import com.toydemo.transaction.dto.TransactionResponse;
import com.toydemo.transaction.exception.DuplicateUniqueIdentifierException;
import com.toydemo.transaction.exception.NoExchangeRateDataException;
import com.toydemo.transaction.exception.ParameterTooLongException;
import com.toydemo.transaction.exception.TransactionNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TransactionService {

    private static final String TREASURY_API_NAME = "TREASURY_API";
    private static final String TRANSACTION_API = "TRANSACTION_API";
    private static final String TREASURY_RATES_PATH = "/v1/accounting/od/rates_of_exchange";

    private final TransactionRepository repository;
    private final TreasuryExchangeRateClient treasuryClient;
    private final ApiRequestLogRepository apiRequestLogRepository;
    private final ObjectMapper objectMapper;
    private final String treasuryApiBaseUrl;

    public TransactionService(TransactionRepository repository,
                              TreasuryExchangeRateClient treasuryClient,
                              ApiRequestLogRepository apiRequestLogRepository,
                              ObjectMapper objectMapper,
                              @Value("${treasury.api.base-url}") String treasuryApiBaseUrl) {
        this.repository = repository;
        this.treasuryClient = treasuryClient;
        this.apiRequestLogRepository = apiRequestLogRepository;
        this.objectMapper = objectMapper;
        this.treasuryApiBaseUrl = treasuryApiBaseUrl;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request,
                                      String requestUrl,
                                      String requestPath) {
        if (request.getDescription() != null && request.getDescription().length() > 50) {
            throw new ParameterTooLongException(request.getDescription().length(), 50, "description");
        }
        if (request.getUniqueIdentifier() != null && request.getUniqueIdentifier().length() > 100) {
            throw new ParameterTooLongException(request.getUniqueIdentifier().length(), 100, "unique identifier");
        }
        if (repository.existsByUniqueIdentifier(request.getUniqueIdentifier())) {
            throw new DuplicateUniqueIdentifierException(request.getUniqueIdentifier());
        }

        Transaction transaction = new Transaction(
                request.getUniqueIdentifier(),
                request.getDescription(),
                request.getTransactionDate(),
                TransactionUtils.roundToCents(request.getPurchaseAmount())
        );

        Transaction saved = repository.save(transaction);
        TransactionResponse response = TransactionResponse.from(saved);

        saveApiRequestLog(saved,
                ApiDirection.INCOMING,
                TRANSACTION_API,
                requestUrl,
                requestPath,
                request,
                response,
                (long) HttpStatus.CREATED.value());

        return response;
    }

    @Transactional
    public TransactionConversionResponse getConversion(String uniqueIdentifier,
                                                        String currency,
                                                        String requestUrl,
                                                        String requestPath) {
        Transaction transaction = repository.findByUniqueIdentifier(uniqueIdentifier)
                .orElseThrow(() -> new TransactionNotFoundException(uniqueIdentifier));

        LocalDate transactionDate = transaction.getTransactionDate();
        LocalDate startWindow = transactionDate.minusMonths(6);
        String treasuryUrl = buildTreasuryRequestUrl(currency, startWindow);

        List<TreasuryExchangeRateRecord> ratesResponse;
        try {
            ratesResponse = treasuryClient.fetchRates(currency, startWindow);
        } catch (RuntimeException ex) {
            saveApiRequestLog(transaction,
                    ApiDirection.OUTGOING,
                    TREASURY_API_NAME,
                    treasuryUrl,
                    TREASURY_RATES_PATH,
                    Map.of("currency", currency, "startWindow", startWindow.toString()),
                    Map.of("error", ex.getMessage()),
                    (long) HttpStatus.NOT_FOUND.value());
            throw ex;
        }

        saveApiRequestLog(transaction,
                ApiDirection.OUTGOING,
                TREASURY_API_NAME,
                treasuryUrl,
                TREASURY_RATES_PATH,
                Map.of("currency", currency, "startWindow", startWindow.toString()),
                ratesResponse,
                (long) HttpStatus.OK.value());

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

        TransactionConversionResponse response = new TransactionConversionResponse(
                transaction.getUniqueIdentifier(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getPurchaseAmount(),
                exchangeRate,
                convertedAmount
        );

        saveApiRequestLog(transaction,
                ApiDirection.INCOMING,
                TRANSACTION_API,
                requestUrl,
                requestPath,
                Map.of("currency", currency),
                response,
                (long) HttpStatus.OK.value());

        return response;
    }

    private void saveApiRequestLog(Transaction transaction,
                                   ApiDirection direction,
                                   String apiName,
                                   String requestUrl,
                                   String requestPath,
                                   Object requestBody,
                                   Object responseBody,
                                   Long responseCode) {
        ApiRequestLog requestLog = new ApiRequestLog(
                transaction,
                direction,
                apiName,
                requestUrl,
                requestPath,
                toJson(requestBody),
                toJson(responseBody),
                LocalDateTime.now(),
                responseCode
        );
        apiRequestLogRepository.save(requestLog);
    }

    private String buildTreasuryRequestUrl(String currency, LocalDate startWindow) {
        String filter = "country_currency_desc:eq:%s,record_date:gte:%s".formatted(currency, startWindow);
        return UriComponentsBuilder.fromUriString(Objects.requireNonNull(treasuryApiBaseUrl, "treasuryApiBaseUrl must not be null"))
                .path(TREASURY_RATES_PATH)
                .queryParam("fields", "country_currency_desc,exchange_rate,record_date")
                .queryParam("filter", filter)
                .queryParam("page[size]", "100")
                .toUriString();
    }

    private String toJson(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String raw) {
            return raw;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            return object.toString();
        }
    }
}
