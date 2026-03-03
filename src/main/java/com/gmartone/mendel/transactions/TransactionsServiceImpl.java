package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.Transaction;
import org.springframework.stereotype.Service;

@Service
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository transactionRepository;

    public TransactionsServiceImpl(TransactionsRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction create(Transaction transaction) {

        // Check if ID already exists
        if (transactionRepository.findById(transaction.id()) != null) {
            throw new IllegalArgumentException("Transaction ID already exists");
        }

        // Optionally validate parent exists
        if (transaction.parent_id() != null) {
            if (transactionRepository.findById(transaction.parent_id()) == null) {
                throw new IllegalArgumentException("Parent transaction not found");
            }
        }

        return transactionRepository.create(transaction);
    }
}