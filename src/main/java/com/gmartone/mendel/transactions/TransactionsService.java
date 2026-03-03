package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;

public interface TransactionsService {

    /**
     * Creates a transaction
     *
     * @param transaction The transaction to be created
     * @return The created transaction
     */
    Transaction create(Transaction transaction);
}
