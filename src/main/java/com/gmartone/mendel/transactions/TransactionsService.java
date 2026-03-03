package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;

import java.util.List;

public interface TransactionsService {

    /**
     * Creates a transaction
     *
     * @param transaction The transaction to be created
     * @return The created transaction
     */
    Transaction create(Transaction transaction);

    /**
     * Retrieves the list of ids related to a transaction type
     * @param type the transaction type
     * @return a list of ids from transactions containing said type
     */
    List<Long> findByType(String type);

    /**
     * Retrieves the total sum of money related to a transaction
     * @param id the transactions id
     * @return the total sum of money related to the transaction (the transaction with the id and its children)
     */
    double sum(long id);
}
