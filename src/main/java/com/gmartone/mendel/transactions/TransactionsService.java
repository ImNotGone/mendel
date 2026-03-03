package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import com.gmartone.mendel.transactions.exception.TransactionNotFoundException;

import java.util.List;

public interface TransactionsService {

    /**
     * Creates a transaction.
     *
     * @param transaction the transaction to persist
     * @return the persisted transaction
     * @throws IllegalArgumentException if the id already exists or the parent_id is not found
     */
    Transaction create(Transaction transaction);

    /**
     * Returns all transaction ids that share the given type.
     *
     * @param type the type to filter on
     * @return list of matching ids (may be empty, never null)
     */
    List<Long> findByType(String type);

    /**
     * Returns the total amount for the given transaction and all its transitive descendants.
     *
     * @param id the root transaction id
     * @return recursive sum
     * @throws TransactionNotFoundException if no transaction exists with the given id
     */
    double sum(long id);
}
