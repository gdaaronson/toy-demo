package com.toydemo.transaction.audit;

import com.toydemo.transaction.Transaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_request_logs")
public class ApiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ApiDirection direction;

    @Column(name = "api_name", nullable = false, length = 64)
    private String apiName;

    @Column(name = "request_url", length = 1024)
    private String requestUrl;

    @Column(name = "request_path", length = 512)
    private String requestPath;

    @Lob
    @Column(name = "request_body")
    private String requestBody;

    @Lob
    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "response_code")
    private Long responseCode;

    protected ApiRequestLog() {
    }

    public ApiRequestLog(Transaction transaction,
                         ApiDirection direction,
                         String apiName,
                         String requestUrl,
                         String requestPath,
                         String requestBody,
                         String responseBody,
                         LocalDateTime createdAt,
                         Long responseCode) {
        this.transaction = transaction;
        this.direction = direction;
        this.apiName = apiName;
        this.requestUrl = requestUrl;
        this.requestPath = requestPath;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
        this.responseCode = responseCode;
    }

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public ApiDirection getDirection() {
        return direction;
    }

    public String getApiName() {
        return apiName;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getResponseCode() {
        return responseCode;
    }
}
