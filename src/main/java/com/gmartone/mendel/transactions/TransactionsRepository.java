package com.gmartone.mendel.transactions;


import com.gmartone.mendel.transactions.dto.Transaction;

public interface TransactionsRepository {

    Transaction findById(long id);

    Transaction create(Transaction transaction);

}
