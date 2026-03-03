package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import com.gmartone.mendel.transactions.exception.TransactionNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
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
 *
 * <h2>Thread safety</h2>
 * {@link #create} is {@code synchronized} at the service level so that the
 * check-then-act sequence (does this id already exist? → write) is atomic across
 * threads. Placing the lock here, rather than inside the repository, keeps the
 * repository simple (a pure data store) and makes the concurrency intent explicit
 * at the business-logic layer where the invariant actually lives.
 */
@Service
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository repository;

    public TransactionsServiceImpl(TransactionsRepository repository) {
        this.repository = repository;
    }

    /**
     * Atomically validates and persists a new transaction.
     *
     * <p>The method is {@code synchronized} so that two concurrent requests for the
     * same id cannot both pass the duplicate-id check and both proceed to write.
     */
    @Override
    public synchronized Transaction create(Transaction transaction) {
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

    /**
     * Returns the sum of the given transaction's amount plus all its transitive
     * descendants, using an iterative Depth-First Search to avoid stack-overflow
     * risks on deep trees and to keep the call stack flat.
     *
     * <p>{@link BigDecimal} is used as the accumulator to avoid the rounding and
     * representation errors inherent to floating-point arithmetic.
     *
     * @throws TransactionNotFoundException if no transaction exists with the given id
     */
    @Override
    public BigDecimal sum(long id) {
        Transaction root = repository.findById(id);
        if (root == null) {
            throw new TransactionNotFoundException(id);
        }

        BigDecimal total = BigDecimal.ZERO;

        Deque<Long> stack = new ArrayDeque<>();
        stack.push(id);

        while (!stack.isEmpty()) {
            long currentId = stack.pop();
            Transaction current = repository.findById(currentId);

            if (current != null) {
                total = total.add(current.amount());
                repository.findChildren(currentId).forEach(stack::push);
            }
        }

        return total;
    }
}
