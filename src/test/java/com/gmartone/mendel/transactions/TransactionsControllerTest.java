package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import com.gmartone.mendel.transactions.exception.GlobalExceptionHandler;
import com.gmartone.mendel.transactions.exception.TransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionsController.class)
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
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

    // -----------------------------------------------------------------------
    // PUT /transactions/{id}
    // -----------------------------------------------------------------------

    @Test
    public void create_shouldReturn201AndOkBody_whenTransactionIsValid() throws Exception {
        Transaction expected = new Transaction(4, 4000, "insurance", 1L);
        when(transactionService.create(any(Transaction.class))).thenReturn(expected);

        mockMvc.perform(put("/transactions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 4000, "type": "insurance", "parent_id": 1 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        { "status": "ok" }
                        """));
    }

    @Test
    public void create_shouldReturn400_whenTransactionIdIsAlreadyInUse() throws Exception {
        when(transactionService.create(any())).thenThrow(new IllegalArgumentException("Transaction id already exists: 1"));

        mockMvc.perform(put("/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 4000, "type": "insurance" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    public void create_shouldReturn404_whenTransactionIdIsNotNumeric() throws Exception {
        mockMvc.perform(put("/transactions/car")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 4000, "type": "insurance" }
                                """))
                .andExpect(status().isNotFound());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void create_shouldReturn400_whenAmountIsMissing() throws Exception {
        // amount defaults to 0.0 when omitted, which violates @Positive
        mockMvc.perform(put("/transactions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "insurance" }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void create_shouldReturn400_whenAmountIsNegative() throws Exception {
        mockMvc.perform(put("/transactions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": -100, "type": "insurance" }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    public void create_shouldReturn400_whenTypeIsBlank() throws Exception {
        mockMvc.perform(put("/transactions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 100, "type": "" }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    // -----------------------------------------------------------------------
    // GET /transactions/types/{type}
    // -----------------------------------------------------------------------

    @Test
    public void findByType_shouldReturnIds_whenTypeIsCar() throws Exception {
        when(transactionService.findByType("car")).thenReturn(List.of(1L));

        mockMvc.perform(get("/transactions/types/car"))
                .andExpect(status().isOk())
                .andExpect(content().json("[1]"));
    }

    @Test
    public void findByType_shouldReturnMultipleIds_whenTypeIsShopping() throws Exception {
        when(transactionService.findByType("shopping")).thenReturn(List.of(2L, 3L));

        mockMvc.perform(get("/transactions/types/shopping"))
                .andExpect(status().isOk())
                .andExpect(content().json("[2, 3]"));
    }

    @Test
    public void findByType_shouldReturnEmptyList_whenTypeDoesNotExist() throws Exception {
        when(transactionService.findByType("insurance")).thenReturn(List.of());

        mockMvc.perform(get("/transactions/types/insurance"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -----------------------------------------------------------------------
    // GET /transactions/sum/{id}
    // -----------------------------------------------------------------------

    @Test
    public void sum_shouldReturnAggregatedSum_whenIdIs1() throws Exception {
        when(transactionService.sum(1)).thenReturn(20000.0);

        mockMvc.perform(get("/transactions/sum/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "sum": 20000 }
                        """));
    }

    @Test
    public void sum_shouldReturn404_whenTransactionDoesNotExist() throws Exception {
        when(transactionService.sum(99)).thenThrow(new TransactionNotFoundException(99));

        mockMvc.perform(get("/transactions/sum/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
