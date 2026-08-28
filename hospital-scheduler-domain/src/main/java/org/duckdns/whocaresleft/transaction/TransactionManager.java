package org.duckdns.whocaresleft.transaction;

public interface TransactionManager {
    <T> T doInTransaction(TransactionCode<T> code);
}
