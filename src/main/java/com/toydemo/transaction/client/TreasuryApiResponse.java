package com.toydemo.transaction.client;

import java.util.List;

public record TreasuryApiResponse(List<TreasuryExchangeRateRecord> data) {
}
