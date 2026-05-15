package com.toydemo.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.errors[0].field").value("description"));
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
}
