package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class TransactionsRepositoryImpl implements TransactionsRepository {
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
        return Collections.emptyList();
    }

    @Override
    public Set<Long> findChildren(long id) {
        return Set.of();
    }
}
