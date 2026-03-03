package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import com.gmartone.mendel.transactions.exception.TransactionNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link TransactionsService}.
 *
 * <p>Business rules enforced here (kept out of the controller and repository
 * to respect the Single-Responsibility principle):
 * <ul>
 *   <li>A transaction id must be unique.</li>
 *   <li>A {@code parent_id}, when supplied, must reference an existing transaction.</li>
 *   <li>Querying the sum of a non-existent transaction is a client error (404).</li>
 * </ul>
 */
@Service
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository repository;

    public TransactionsServiceImpl(TransactionsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction create(Transaction transaction) {
        if (repository.findById(transaction.id()) != null) {
            throw new IllegalArgumentException("Transaction id already exists: " + transaction.id());
        }

        if (transaction.parent_id() != null &&
                repository.findById(transaction.parent_id()) == null) {
            throw new IllegalArgumentException("Parent transaction not found: " + transaction.parent_id());
        }

        return repository.create(transaction);
    }

    @Override
    public List<Long> findByType(String type) {
        return repository.findByType(type);
    }

    @Override
    public double sum(long id) {
        if (repository.findById(id) == null) {
            throw new TransactionNotFoundException(id);
        }
        return sumRecursive(id);
    }

    private double sumRecursive(long id) {
        Transaction transaction = repository.findById(id);
        if (transaction == null) {
            return 0.0;
        }

        double total = transaction.amount();
        for (Long childId : repository.findChildren(id)) {
            total += sumRecursive(childId);
        }
        return total;
    }
}
