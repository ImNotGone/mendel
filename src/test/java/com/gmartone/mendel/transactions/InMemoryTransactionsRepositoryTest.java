package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionsRepositoryTest {

    private InMemoryTransactionsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionsRepository();
    }

    // -------------------------
    // CREATE + FIND BY ID
    // -------------------------

    @Test
    void create_shouldStoreTransaction() {

        Transaction tx = new Transaction(1L, 5000, "car", null);

        repository.create(tx);

        Transaction found = repository.findById(1L);

        assertNotNull(found);
        assertEquals(5000, found.amount());
        assertEquals("car", found.type());
    }

    @Test
    void findById_shouldReturnNull_whenTransactionDoesNotExist() {

        Transaction found = repository.findById(999L);

        assertNull(found);
    }

    // -------------------------
    // FIND BY TYPE
    // -------------------------

    @Test
    void findByType_shouldReturnEmptySet_whenNoTransactionsWithType() {

        List<Long> result = repository.findByType("shopping");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByType_shouldReturnIdsForGivenType() {

        repository.create(new Transaction(1L, 5000, "car", null));
        repository.create(new Transaction(2L, 6000, "car", null));
        repository.create(new Transaction(3L, 7000, "shopping", null));

        List<Long> result = repository.findByType("car");

        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }

    // -------------------------
    // CHILDREN INDEX
    // -------------------------

    @Test
    void findChildren_shouldReturnEmptySet_whenNoChildren() {

        repository.create(new Transaction(1L, 5000, "car", null));

        Set<Long> children = repository.findChildren(1L);

        assertNotNull(children);
        assertTrue(children.isEmpty());
    }

    @Test
    void findChildren_shouldReturnChildIds() {

        repository.create(new Transaction(1L, 5000, "car", null));
        repository.create(new Transaction(2L, 3000, "shopping", 1L));
        repository.create(new Transaction(3L, 2000, "shopping", 1L));

        Set<Long> children = repository.findChildren(1L);

        assertEquals(2, children.size());
        assertTrue(children.contains(2L));
        assertTrue(children.contains(3L));
    }

    // -------------------------
    // DATA INTEGRITY
    // -------------------------

    @Test
    void create_shouldIndexByTypeAndParentCorrectly() {

        repository.create(new Transaction(1L, 5000, "car", null));
        repository.create(new Transaction(2L, 3000, "shopping", 1L));

        assertEquals(1, repository.findByType("car").size());
        assertEquals(1, repository.findChildren(1L).size());
    }
}