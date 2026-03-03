package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryTransactionsRepository implements TransactionsRepository {

    private final Map<Long, Transaction> byId = new HashMap<>();
    private final Map<String, Set<Long>> byType = new HashMap<>();
    private final Map<Long, Set<Long>> childrenByParent = new HashMap<>();

    @Override
    public Transaction findById(long id) {
        return byId.get(id);
    }

    @Override
    public Transaction create(Transaction transaction) {

        byId.put(transaction.id(), transaction);

        // index by type
        byType
                .computeIfAbsent(transaction.type(), k -> new HashSet<>())
                .add(transaction.id());

        // index by parent
        if (transaction.parent_id() != null) {
            childrenByParent
                    .computeIfAbsent(transaction.parent_id(), k -> new HashSet<>())
                    .add(transaction.id());
        }

        return transaction;
    }

    public List<Long> findByType(String type) {
        return byType.getOrDefault(type, Collections.emptySet()).stream().toList();
    }

    public Set<Long> findChildren(long parentId) {
        return childrenByParent.getOrDefault(parentId, Collections.emptySet());
    }
}