package com.toydemo.transaction.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record TreasuryExchangeRateRecord(
        @JsonProperty("country_currency_desc") String countryCurrencyDesc,
        @JsonProperty("exchange_rate") String exchangeRate,
        @JsonProperty("record_date") LocalDate recordDate
) {
}
