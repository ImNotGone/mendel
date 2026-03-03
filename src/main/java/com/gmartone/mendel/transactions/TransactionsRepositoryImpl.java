package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.springframework.stereotype.Repository;

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
}
