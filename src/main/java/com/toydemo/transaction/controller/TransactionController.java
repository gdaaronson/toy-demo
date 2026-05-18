package com.toydemo.transaction.controller;

import com.toydemo.transaction.dto.CreateTransactionRequest;
import com.toydemo.transaction.dto.TransactionConversionResponse;
import com.toydemo.transaction.dto.TransactionResponse;
import com.toydemo.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody CreateTransactionRequest request,
                                      HttpServletRequest servletRequest) {
        return service.create(request,
                buildRequestUrl(servletRequest),
                servletRequest.getRequestURI());
    }

    @GetMapping("/{uniqueIdentifier}")
    public TransactionConversionResponse getConversion(
            @PathVariable String uniqueIdentifier,
            @RequestParam @NotBlank(message = "currency is required") String currency,
            HttpServletRequest servletRequest
    ) {
        return service.getConversion(uniqueIdentifier,
                currency,
                buildRequestUrl(servletRequest),
                servletRequest.getRequestURI());
    }

    private String buildRequestUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        return queryString == null ? url : url + "?" + queryString;
    }
}
