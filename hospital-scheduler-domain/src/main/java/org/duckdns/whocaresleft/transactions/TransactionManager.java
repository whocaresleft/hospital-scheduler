package org.duckdns.whocaresleft.transactions;

public interface TransactionManager {
    <T> T doInTransaction(TransactionCode<T> code);
}
