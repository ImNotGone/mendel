package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionsRepositoryTest {

    private InMemoryTransactionsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionsRepository();
    }

    // -----------------------------------------------------------------------
    // create + findById
    // -----------------------------------------------------------------------

    @Test
    void create_shouldStoreTransaction() {
        Transaction tx = new Transaction(1L, new BigDecimal("5000"), "car", null);
        repository.create(tx);

        Transaction found = repository.findById(1L);

        assertNotNull(found);
        assertEquals(new BigDecimal("5000"), found.amount());
        assertEquals("car", found.type());
    }

    @Test
    void findById_shouldReturnNull_whenTransactionDoesNotExist() {
        assertNull(repository.findById(999L));
    }

    // -----------------------------------------------------------------------
    // findByType
    // -----------------------------------------------------------------------

    @Test
    void findByType_shouldReturnEmptyList_whenNoTransactionsWithType() {
        List<Long> result = repository.findByType("shopping");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByType_shouldReturnIdsForGivenType() {
        repository.create(new Transaction(1L, new BigDecimal("5000"), "car", null));
        repository.create(new Transaction(2L, new BigDecimal("6000"), "car", null));
        repository.create(new Transaction(3L, new BigDecimal("7000"), "shopping", null));

        List<Long> result = repository.findByType("car");

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(1L, 2L)));
    }

    // -----------------------------------------------------------------------
    // findChildren
    // -----------------------------------------------------------------------

    @Test
    void findChildren_shouldReturnEmptySet_whenNoChildren() {
        repository.create(new Transaction(1L, new BigDecimal("5000"), "car", null));

        Set<Long> children = repository.findChildren(1L);

        assertNotNull(children);
        assertTrue(children.isEmpty());
    }

    @Test
    void findChildren_shouldReturnChildIds() {
        repository.create(new Transaction(1L, new BigDecimal("5000"), "car", null));
        repository.create(new Transaction(2L, new BigDecimal("3000"), "shopping", 1L));
        repository.create(new Transaction(3L, new BigDecimal("2000"), "shopping", 1L));

        Set<Long> children = repository.findChildren(1L);

        assertEquals(2, children.size());
        assertTrue(children.containsAll(Set.of(2L, 3L)));
    }

    // -----------------------------------------------------------------------
    // index integrity
    // -----------------------------------------------------------------------

    @Test
    void create_shouldIndexByTypeAndParentCorrectly() {
        repository.create(new Transaction(1L, new BigDecimal("5000"), "car", null));
        repository.create(new Transaction(2L, new BigDecimal("3000"), "shopping", 1L));

        assertEquals(1, repository.findByType("car").size());
        assertEquals(1, repository.findChildren(1L).size());
    }
}
