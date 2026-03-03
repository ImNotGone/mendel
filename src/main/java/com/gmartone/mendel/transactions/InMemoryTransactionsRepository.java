package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;

import java.util.List;
import java.util.Set;

public class InMemoryTransactionsRepository implements TransactionsRepository {
    @Override
    public Transaction findById(long id) {
        return null;
    }

    @Override
    public Transaction create(Transaction transaction) {
        return null;
    }

    @Override
    public List<Long> findByType(String type) {
        return List.of();
    }

    @Override
    public Set<Long> findChildren(long id) {
        return Set.of();
    }
}
