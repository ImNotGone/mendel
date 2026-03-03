package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.ArrayList;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionsController.class)
@AutoConfigureMockMvc
public class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionsService transactionService;

    List<Transaction> transactions = new ArrayList<>();

    @BeforeEach
    public void setup() {
        transactions = List.of(
                new Transaction(1, 5000, "car", null),
                new Transaction(2, 10000, "shopping", 1L),
                new Transaction(3, 5000, "shopping", 2L)
        );
    }

    // REST API

    @Test
    public void shouldCreateTransaction() throws Exception {
        String jsonBody = """
                {
                    "amount": 4000,
                    "type": "insurance",
                    "parent_id": 1
                }""";

        String jsonResponse = """
                {
                    "status": "ok"
                }""";

        Transaction expectedTransaction = new Transaction(4, 4000, "insurance", 1L);
        when(transactionService.create(Mockito.any(Transaction.class))).thenReturn(expectedTransaction);

        mockMvc.perform(put("/transactions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    public void shouldFailCreateTransactionWhenIdIsInUse() throws Exception {
        String jsonBody = """
                {
                    "amount": 4000,
                    "type": "insurance",
                    "parent_id": 1
                }""";
        when(transactionService.create(Mockito.any(Transaction.class))).thenThrow(new IllegalArgumentException());
        mockMvc.perform(put("/transactions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailCreateTransactionWhenIdIsNan() throws Exception {
        String jsonBody = """
                {
                    "amount": 4000,
                    "type": "insurance",
                    "parent_id": 1
                }""";

        mockMvc.perform(put("/transactions/car")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        ).andExpect(status().isNotFound());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldFailCreateTransactionWhenBodyIsInvalid() throws Exception {
        String jsonBody = """
                {
                    "type": "insurance",
                    "parent_id": 1
                }""";
        mockMvc.perform(put("/transactions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                )
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }

    @Test
    public void shouldRetrieveTransactionsListForTypeCar() throws Exception {
        String jsonResponse = """
                [
                    1
                ]""";

        List<Long> expectedIds = List.of(1L);
        when(transactionService.findByType("car")).thenReturn(expectedIds);
        mockMvc.perform(get("/transactions/type/car"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    public void shouldRetrieveTransactionsListForTypeShopping() throws Exception {
        String jsonResponse = """
                [
                    2,
                    3
                ]""";

        List<Long> expectedIds = List.of(2L, 3L);
        when(transactionService.findByType("shopping")).thenReturn(expectedIds);

        mockMvc.perform(get("/transactions/type/shopping"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    public void shouldRetrieveEmptyTransactionsListForTypeInsurance() throws Exception {
        String jsonResponse = "[]";
        mockMvc.perform(get("/transactions/type/insurance"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    public void shouldGetTotalSumOfTransactionsFollowingTheChildrenFor1() throws Exception {
        String jsonResponse = """
                {
                  sum: 20000
                }
                """;
        mockMvc.perform(get("/transactions/sum/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    public void shouldGetTotalSumOfTransactionsFollowingTheChildrenFor2() throws Exception {
        String jsonResponse = "{sum: 15000}";
        mockMvc.perform(get("/transactions/sum/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }
}
