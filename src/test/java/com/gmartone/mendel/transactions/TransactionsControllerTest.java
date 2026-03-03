package com.gmartone.mendel.transactions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionsController.class)
@AutoConfigureMockMvc
public class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                    "id": 4,
                    "amount": 4000,
                    "type": "insurance",
                    "parent_id": 1
                }""";
        mockMvc.perform(post("/transactions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    public void shouldFailCreateTransactionWhenIdIsInUse() throws Exception {
        mockMvc.perform(post("/transactions/1")).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailCreateTransactionWhenIdIsNan() throws Exception {
        mockMvc.perform(post("/transactions/car")).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailCreateTransactionWhenIdIsNull() throws Exception {
        mockMvc.perform(post("/transactions/")).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailCreateTransactionWhenBodyIsInvalid() throws Exception {
        String jsonBody = """
                {
                    "type": "insurance",
                    "parent_id": 1
                }""";
        mockMvc.perform(post("/transactions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRetrieveTransactionsListForTypeCar() throws Exception {
        String jsonResponse = """
                [
                    1
                ]""";

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
        String jsonResponse = "{sum: 20000}";
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
