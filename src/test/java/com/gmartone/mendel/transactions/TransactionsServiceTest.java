package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import com.gmartone.mendel.transactions.exception.TransactionNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionsServiceTest {

    @Mock
    private TransactionsRepository repository;

    @InjectMocks
    private TransactionsServiceImpl service;

    // -----------------------------------------------------------------------
    // create
    // -----------------------------------------------------------------------

    @Test
    void create_shouldThrowException_whenTransactionAlreadyExists() {
        Transaction tx = new Transaction(1, new BigDecimal("1000"), "car", null);
        when(repository.findById(1L)).thenReturn(tx);

        assertThrows(IllegalArgumentException.class, () -> service.create(tx));
    }

    @Test
    void create_shouldThrowException_whenParentIdDoesNotExist() {
        Transaction tx = new Transaction(2, new BigDecimal("1000"), "car", 99L);
        when(repository.findById(2L)).thenReturn(null);
        when(repository.findById(99L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.create(tx));
    }

    @Test
    void create_shouldPersistTransaction_whenValid() {
        Transaction tx = new Transaction(1, new BigDecimal("1000"), "car", null);
        when(repository.findById(1L)).thenReturn(null);
        when(repository.create(tx)).thenReturn(tx);

        Transaction result = service.create(tx);

        assertEquals(1L, result.id());
        verify(repository).create(tx);
    }

    @Test
    void create_shouldPersistTransaction_whenParentExists() {
        Transaction parent = new Transaction(1, new BigDecimal("500"), "car", null);
        Transaction child  = new Transaction(2, new BigDecimal("200"), "car", 1L);

        when(repository.findById(2L)).thenReturn(null);
        when(repository.findById(1L)).thenReturn(parent);
        when(repository.create(child)).thenReturn(child);

        Transaction result = service.create(child);

        assertEquals(2L, result.id());
    }

    // -----------------------------------------------------------------------
    // findByType
    // -----------------------------------------------------------------------

    @Test
    void findByType_shouldReturnEmptyList_whenNoTransactionHasThatType() {
        when(repository.findByType("car")).thenReturn(Collections.emptyList());

        List<Long> result = service.findByType("car");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByType_shouldReturnMatchingIds() {
        when(repository.findByType("car")).thenReturn(List.of(1L, 2L));

        List<Long> result = service.findByType("car");

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(1L, 2L)));
    }

    // -----------------------------------------------------------------------
    // sum – iterative DFS
    // -----------------------------------------------------------------------

    @Test
    void sum_shouldThrowTransactionNotFoundException_whenTransactionDoesNotExist() {
        when(repository.findById(1L)).thenReturn(null);

        assertThrows(TransactionNotFoundException.class, () -> service.sum(1L));
    }

    @Test
    void sum_shouldReturnAmount_whenTransactionHasNoChildren() {
        Transaction tx = new Transaction(1, new BigDecimal("5000"), "car", null);
        when(repository.findById(1L)).thenReturn(tx);
        when(repository.findChildren(1L)).thenReturn(Collections.emptySet());

        assertEquals(new BigDecimal("5000"), service.sum(1L));
    }

    @Test
    void sum_shouldReturnAggregatedAmount_whenTransactionHasDirectChildren() {
        Transaction parent = new Transaction(1, new BigDecimal("5000"), "car", null);
        Transaction child  = new Transaction(2, new BigDecimal("10000"), "shopping", 1L);

        when(repository.findById(1L)).thenReturn(parent);
        when(repository.findById(2L)).thenReturn(child);
        when(repository.findChildren(1L)).thenReturn(Set.of(2L));
        when(repository.findChildren(2L)).thenReturn(Collections.emptySet());

        assertEquals(new BigDecimal("15000"), service.sum(1L));
    }

    @Test
    void sum_shouldReturnAggregatedAmount_forDeepHierarchy() {
        Transaction t1 = new Transaction(1, new BigDecimal("1000"), "a", null);
        Transaction t2 = new Transaction(2, new BigDecimal("2000"), "a", 1L);
        Transaction t3 = new Transaction(3, new BigDecimal("3000"), "a", 2L);

        when(repository.findById(1L)).thenReturn(t1);
        when(repository.findById(2L)).thenReturn(t2);
        when(repository.findById(3L)).thenReturn(t3);
        when(repository.findChildren(1L)).thenReturn(Set.of(2L));
        when(repository.findChildren(2L)).thenReturn(Set.of(3L));
        when(repository.findChildren(3L)).thenReturn(Collections.emptySet());

        assertEquals(new BigDecimal("6000"), service.sum(1L));
    }

    @Test
    void sum_shouldReturnAggregatedAmount_forWideTree() {
        // t1 has two direct children – tests that DFS visits all branches
        Transaction t1 = new Transaction(1, new BigDecimal("100"), "a", null);
        Transaction t2 = new Transaction(2, new BigDecimal("200"), "a", 1L);
        Transaction t3 = new Transaction(3, new BigDecimal("300"), "a", 1L);

        when(repository.findById(1L)).thenReturn(t1);
        when(repository.findById(2L)).thenReturn(t2);
        when(repository.findById(3L)).thenReturn(t3);
        when(repository.findChildren(1L)).thenReturn(Set.of(2L, 3L));
        when(repository.findChildren(2L)).thenReturn(Collections.emptySet());
        when(repository.findChildren(3L)).thenReturn(Collections.emptySet());

        assertEquals(new BigDecimal("600"), service.sum(1L));
    }

    @Test
    void sum_shouldHandleDecimalAmountsPrecisely() {
        Transaction t1 = new Transaction(1, new BigDecimal("0.10"), "a", null);
        Transaction t2 = new Transaction(2, new BigDecimal("0.20"), "a", 1L);

        when(repository.findById(1L)).thenReturn(t1);
        when(repository.findById(2L)).thenReturn(t2);
        when(repository.findChildren(1L)).thenReturn(Set.of(2L));
        when(repository.findChildren(2L)).thenReturn(Collections.emptySet());

        // 0.10 + 0.20 = 0.30 exactly – would be 0.30000000000000004 with double
        assertEquals(new BigDecimal("0.30"), service.sum(1L));
    }
}
