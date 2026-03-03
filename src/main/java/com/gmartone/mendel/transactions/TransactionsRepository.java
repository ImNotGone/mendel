package com.gmartone.mendel.transactions;


import com.gmartone.mendel.transactions.dto.Transaction;

import java.util.Set;

public interface TransactionsRepository {

    Transaction findById(long id);

    Transaction create(Transaction transaction);

    int findByType(String type);

    Set<Long> findChildren(long id);
}
