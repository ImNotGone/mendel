package com.gmartone.mendel.transactions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that spin up the full Spring application context and exercise
 * the complete request-response cycle via {@link MockMvc}.
 *
 * <p>{@code @DirtiesContext} resets the in-memory store between tests so that each
 * test is fully independent and order-agnostic.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // -----------------------------------------------------------------------
    // Happy-path flow from the challenge spec
    // -----------------------------------------------------------------------

    @Test
    void specExample_fullFlow_shouldWorkCorrectly() throws Exception {

        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 5000, "type": "cars" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        { "status": "ok" }
                        """));

        mockMvc.perform(put("/transactions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 10000, "type": "shopping", "parent_id": 10 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/transactions/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 5000, "type": "shopping", "parent_id": 11 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(content().json("[10]"));

        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "sum": 20000 }
                        """));

        mockMvc.perform(get("/transactions/sum/11"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "sum": 15000 }
                        """));
    }

    // -----------------------------------------------------------------------
    // Error cases
    // -----------------------------------------------------------------------

    @Test
    void create_shouldReturn400_whenIdIsAlreadyInUse() throws Exception {
        String body = """
                { "amount": 100, "type": "test" }
                """;

        mockMvc.perform(put("/transactions/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/transactions/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenParentIdDoesNotExist() throws Exception {
        mockMvc.perform(put("/transactions/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 100, "type": "test", "parent_id": 999 }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenAmountIsZero() throws Exception {
        mockMvc.perform(put("/transactions/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 0, "type": "test" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenAmountIsNegative() throws Exception {
        mockMvc.perform(put("/transactions/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": -50, "type": "test" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenTypeIsBlank() throws Exception {
        mockMvc.perform(put("/transactions/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 100, "type": "" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sum_shouldReturn404_whenTransactionDoesNotExist() throws Exception {
        mockMvc.perform(get("/transactions/sum/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByType_shouldReturnEmptyList_whenTypeDoesNotExist() throws Exception {
        mockMvc.perform(get("/transactions/types/unknown"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
