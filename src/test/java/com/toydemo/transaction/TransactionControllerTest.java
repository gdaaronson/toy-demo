package com.toydemo.transaction;

import com.toydemo.transaction.client.TreasuryExchangeRateClient;
import com.toydemo.transaction.client.TreasuryExchangeRateRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:toydemo;DB_CLOSE_DELAY=-1")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TreasuryExchangeRateClient treasuryExchangeRateClient;

    @Test
    void createsTransactionWhenValid() throws Exception {
        String body = """
                {
                  "description": "Coffee",
                  "transactionDate": "%s",
                  "purchaseAmount": 4.50,
                  "uniqueIdentifier": "txn-001"
                }
                """.formatted(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uniqueIdentifier").value("txn-001"))
                .andExpect(jsonPath("$.purchaseAmount").value(4.50));
    }

    @Test
    void rejectsDuplicateUniqueIdentifier() throws Exception {
        String body = """
                {
                  "description": "Coffee",
                  "transactionDate": "%s",
                  "purchaseAmount": 4.50,
                  "uniqueIdentifier": "txn-dup"
                }
                """.formatted(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("uniqueIdentifier"));
    }

    @Test
    void rejectsUniqueIdentifierTooLong() throws Exception {
        String longIdentifier = "x".repeat(101);
        String body = """
                {
                  "description": "Coffee",
                  "transactionDate": "%s",
                  "purchaseAmount": 4.50,
                  "uniqueIdentifier": "%s"
                }
                """.formatted(LocalDate.now().minusDays(1), longIdentifier);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("uniqueIdentifier"))
                .andExpect(jsonPath("$.errors[0].message").value("unique identifier must be 100 characters or fewer"));
    }

    @Test
    void rejectsDescriptionTooLong() throws Exception {
        String body = """
                {
                  "description": "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz",
                  "transactionDate": "%s",
                  "purchaseAmount": 1.00,
                  "uniqueIdentifier": "txn-long-desc"
                }
                """.formatted(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("description"))
                .andExpect(jsonPath("$.errors[0].message").value("description is too long: 52 characters (max 50)"));
    }

    @Test
    void acceptsTransactionDateToday() throws Exception {
        String body = """
                {
                  "description": "Today",
                  "transactionDate": "%s",
                  "purchaseAmount": 1.00,
                  "uniqueIdentifier": "txn-today"
                }
                """.formatted(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionDate").value(LocalDate.now().toString()));
    }

    @Test
    void rejectsFutureTransactionDate() throws Exception {
        String body = """
                {
                  "description": "Future",
                  "transactionDate": "%s",
                  "purchaseAmount": 1.00,
                  "uniqueIdentifier": "txn-future"
                }
                """.formatted(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("transactionDate"))
                .andExpect(jsonPath("$.errors[0].message").value("transaction date cannot be in the future"));
    }

    @Test
    void roundsPurchaseAmountToNearestCent() throws Exception {
        String body = """
                {
                  "description": "Rounding",
                  "transactionDate": "%s",
                  "purchaseAmount": 10.999,
                  "uniqueIdentifier": "txn-round-up"
                }
                """.formatted(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.purchaseAmount").value(11.00));
    }

    @Test
    void rejectsAmountThatRoundsToZero() throws Exception {
        String body = """
                {
                  "description": "Tiny",
                  "transactionDate": "%s",
                  "purchaseAmount": 0.004,
                  "uniqueIdentifier": "txn-tiny"
                }
                """.formatted(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("purchaseAmount"));
    }

    @Test
    void rejectsNonPositiveAmount() throws Exception {
        String body = """
                {
                  "description": "Zero",
                  "transactionDate": "%s",
                  "purchaseAmount": 0.00,
                  "uniqueIdentifier": "txn-zero"
                }
                """.formatted(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("purchaseAmount"));
    }

    @Test
    void returnsNotFoundWhenTransactionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/transactions/missing-id")
                        .param("currency", "Canada-Dollar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("transaction not found: missing-id"));
    }

    @Test
    void returnsNotFoundWhenNoExchangeRatesInWindow() throws Exception {
        LocalDate transactionDate = LocalDate.of(2025, 8, 15);
        createTransaction("txn-no-rates", transactionDate, 10.00);

        when(treasuryExchangeRateClient.fetchRates(eq("Canada-Dollar"), eq(transactionDate.minusMonths(6))))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/transactions/txn-no-rates")
                        .param("currency", "Canada-Dollar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("no exchange rate data found for currency Canada-Dollar"));
    }

    @Test
    void returnsConversionWithClosestExchangeRate() throws Exception {
        LocalDate transactionDate = LocalDate.of(2025, 8, 15);
        createTransaction("txn-conv", transactionDate, 10.00);

        when(treasuryExchangeRateClient.fetchRates(eq("Canada-Dollar"), eq(transactionDate.minusMonths(6))))
                .thenReturn(List.of(
                        new TreasuryExchangeRateRecord("Canada-Dollar", "1.350", LocalDate.of(2025, 3, 31)),
                        new TreasuryExchangeRateRecord("Canada-Dollar", "1.400", LocalDate.of(2025, 9, 30))
                ));

        mockMvc.perform(get("/api/transactions/txn-conv")
                        .param("currency", "Canada-Dollar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueIdentifier").value("txn-conv"))
                .andExpect(jsonPath("$.description").value("Coffee"))
                .andExpect(jsonPath("$.transactionDate").value("2025-08-15"))
                .andExpect(jsonPath("$.purchaseAmount").value(10.00))
                .andExpect(jsonPath("$.exchangeRate").value(1.350))
                .andExpect(jsonPath("$.convertedAmount").value(13.50));
    }

    @Test
    void returnsServiceUnavailableWhenTreasuryApiFails() throws Exception {
        LocalDate transactionDate = LocalDate.of(2025, 8, 15);
        createTransaction("txn-api-fail", transactionDate, 10.00);

        when(treasuryExchangeRateClient.fetchRates(eq("Canada-Dollar"), eq(transactionDate.minusMonths(6))))
                .thenThrow(new com.toydemo.transaction.exception.TreasuryApiUnavailableException());

        mockMvc.perform(get("/api/transactions/txn-api-fail")
                        .param("currency", "Canada-Dollar"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("treasury api unavailable"));
    }

    @Test
    void rejectsMissingCurrencyParameter() throws Exception {
        mockMvc.perform(get("/api/transactions/txn-001"))
                .andExpect(status().isBadRequest());
    }

    private void createTransaction(String uniqueIdentifier, LocalDate transactionDate, double amount) throws Exception {
        String body = """
                {
                  "description": "Coffee",
                  "transactionDate": "%s",
                  "purchaseAmount": %.2f,
                  "uniqueIdentifier": "%s"
                }
                """.formatted(transactionDate, amount, uniqueIdentifier);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
