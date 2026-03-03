package com.gmartone.mendel.transactions.exception;

/**
 * Thrown when a requested transaction id does not exist in the repository.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(long id) {
        super("Transaction not found: " + id);
    }
}
