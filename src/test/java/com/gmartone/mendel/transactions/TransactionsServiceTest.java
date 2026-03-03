package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionsServiceTest {

    @Mock
    private TransactionsRepository repository;

    @InjectMocks
    private TransactionsServiceImpl service;

    // -------------------------
    // CREATE
    // -------------------------

    @Test
    void createTransaction_shouldThrowException_whenTransactionAlreadyExists() {

        Transaction tx = new Transaction(1, 1000, "car", null);

        when(repository.findById(1L)).thenReturn(tx);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(tx));
    }

    @Test
    void createTransaction_shouldThrowException_whenParentIdIsNotFound() {

        Transaction tx = new Transaction(2, 1000, "car", 99L);

        when(repository.findById(2L)).thenReturn(null);
        when(repository.findById(99L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(tx));
    }

    @Test
    void createTransaction_shouldThrowException_whenTransactionTypeIsMissing() {

        Transaction tx = new Transaction(1, 1000, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(tx));
    }

    @Test
    void createTransaction_shouldCreateTransaction() {

        Transaction tx = new Transaction(1, 1000, "car", null);

        when(repository.findById(1L)).thenReturn(null);
        when(repository.create(tx)).thenReturn(tx);

        Transaction result = service.create(tx);

        assertEquals(1L, result.id());
        verify(repository).create(tx);
    }

    // -------------------------
    // FIND BY TYPE
    // -------------------------

    @Test
    void findByType_shouldThrowException_whenTransactionTypeIsMissing() {

        assertThrows(IllegalArgumentException.class,
                () -> service.findByType(null));
    }

    @Test
    void findByType_shouldReturnEmptyList_whenNoTransactionHasThatType() {

        when(repository.findByType("car"))
                .thenReturn(Collections.emptyList());

        List<Long> result = service.findByType("car");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByType_shouldReturnTransactionList() {

        when(repository.findByType("car"))
                .thenReturn(List.of(1L, 2L));

        List<Long> result = service.findByType("car");

        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }

    // -------------------------
    // SUM
    // -------------------------

    @Test
    void sum_shouldReturn0_whenTransactionIsNotFound() {

        when(repository.findById(1L)).thenReturn(null);

        double result = service.sum(1L);

        assertEquals(0.0, result);
    }

    @Test
    void sum_shouldReturnCurrentSum_whenTransactionHasNoChildren() {

        Transaction tx = new Transaction(1, 5000, "car", null);

        when(repository.findById(1L)).thenReturn(tx);
        when(repository.findChildren(1L)).thenReturn(Collections.emptySet());

        double result = service.sum(1L);

        assertEquals(5000.0, result);
    }

    @Test
    void sum_shouldReturnAggregatedSum_whenTransactionHasChildren() {

        Transaction parent = new Transaction(1, 5000, "car", null);
        Transaction child = new Transaction(2, 10000, "shopping", 1L);

        when(repository.findById(1L)).thenReturn(parent);
        when(repository.findById(2L)).thenReturn(child);

        when(repository.findChildren(1L)).thenReturn(Set.of(2L));
        when(repository.findChildren(2L)).thenReturn(Collections.emptySet());

        double result = service.sum(1L);

        assertEquals(15000.0, result);
    }
}