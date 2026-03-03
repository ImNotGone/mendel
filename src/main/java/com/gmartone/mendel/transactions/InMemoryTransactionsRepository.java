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
 * <p>All maps use {@link ConcurrentHashMap} to allow safe concurrent reads.
 * The duplicate-id invariant and the write-ordering guarantee are enforced by
 * {@link TransactionsServiceImpl#create}, which holds a monitor lock for the
 * full check-then-act sequence before delegating here.
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

    @Override
    public Transaction create(Transaction transaction) {
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
