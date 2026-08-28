package org.duckdns.whocaresleft.transactions;

import java.util.function.Function;

import org.duckdns.whocaresleft.repository.RepositoryProvider;

@FunctionalInterface
public interface TransactionCode<T> extends Function<RepositoryProvider, T> { }