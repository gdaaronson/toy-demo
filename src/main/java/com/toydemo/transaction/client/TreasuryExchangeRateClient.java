package com.toydemo.transaction.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
public class TreasuryExchangeRateClient {

    private static final String RATES_PATH = "/v1/accounting/od/rates_of_exchange";

    private final RestClient restClient;

    public TreasuryExchangeRateClient(RestClient treasuryRestClient) {
        this.restClient = treasuryRestClient;
    }

    public List<TreasuryExchangeRateRecord> fetchRates(String currency, LocalDate windowStart) {
        String filter = "country_currency_desc:eq:%s,record_date:gte:%s".formatted(currency, windowStart);

        TreasuryApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(RATES_PATH)
                        .queryParam("fields", "country_currency_desc,exchange_rate,record_date")
                        .queryParam("filter", filter)
                        .queryParam("page[size]", "100")
                        .build())
                .retrieve()
                .body(TreasuryApiResponse.class);

        if (response == null || response.data() == null) {
            return List.of();
        }
        return response.data();
    }
}
