package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, in-memory implementation of {@link TransactionsRepository}.
 *
 * <p>Three internal indexes are maintained for O(1) look-ups:
 * <ul>
 *   <li>{@code byId} – primary store keyed by transaction id.</li>
 *   <li>{@code byType} – maps each type string to the set of ids that share it.</li>
 *   <li>{@code childrenByParent} – maps each parent id to the set of its direct children.</li>
 * </ul>
 *
 * <p>All maps use {@link ConcurrentHashMap} so that concurrent HTTP requests do not
 * corrupt shared state. The compound read-then-write in {@link #create} is
 * coordinated via {@code synchronized} to guarantee atomicity.
 */
@Repository
public class InMemoryTransactionsRepository implements TransactionsRepository {

    private final Map<Long, Transaction> byId = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> byType = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> childrenByParent = new ConcurrentHashMap<>();

    @Override
    public Transaction findById(long id) {
        return byId.get(id);
    }

    /**
     * Atomically persists a transaction and updates all secondary indexes.
     *
     * <p>The method is {@code synchronized} so that the check-then-act sequence
     * performed by the service layer (findById → create) remains consistent even
     * under concurrent load. A finer-grained lock per id would also work but
     * adds complexity without measurable benefit for this use-case.
     */
    @Override
    public synchronized Transaction create(Transaction transaction) {
        byId.put(transaction.id(), transaction);

        byType
                .computeIfAbsent(transaction.type(), k -> ConcurrentHashMap.newKeySet())
                .add(transaction.id());

        if (transaction.parent_id() != null) {
            childrenByParent
                    .computeIfAbsent(transaction.parent_id(), k -> ConcurrentHashMap.newKeySet())
                    .add(transaction.id());
        }

        return transaction;
    }

    @Override
    public List<Long> findByType(String type) {
        return byType.getOrDefault(type, Collections.emptySet()).stream().toList();
    }

    @Override
    public Set<Long> findChildren(long parentId) {
        return childrenByParent.getOrDefault(parentId, Collections.emptySet());
    }
}
