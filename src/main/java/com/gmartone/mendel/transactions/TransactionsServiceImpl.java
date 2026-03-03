package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository repository;

    public TransactionsServiceImpl(TransactionsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction create(Transaction transaction) {

        if (repository.findById(transaction.id()) != null) {
            throw new IllegalArgumentException("ID already exists");
        }

        if (transaction.parent_id() != null &&
                repository.findById(transaction.parent_id()) == null) {
            throw new IllegalArgumentException("Parent not found");
        }

        return repository.create(transaction);
    }

    @Override
    public List<Long> findByType(String type) {
        return new ArrayList<>(repository.findByType(type));
    }

    @Override
    public double sum(long id) {
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