package org.duckdns.whocaresleft.transaction.mariadb;

import org.duckdns.whocaresleft.repository.RepositoryProvider;
import org.duckdns.whocaresleft.repository.mariadb.MariaRepositoryProvider;
import org.duckdns.whocaresleft.transaction.TransactionCode;
import org.duckdns.whocaresleft.transaction.TransactionManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class MariaTransactionManager implements TransactionManager {
    
    private final EntityManagerFactory entityManagerFactory;
    
    public MariaTransactionManager(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
    
    @Override
    public <T> T doInTransaction(TransactionCode<T> code) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction t = em.getTransaction();
        
        RepositoryProvider provider = new MariaRepositoryProvider(em);
        
        T result;
        try {
            t.begin();
            result = code.apply(provider);
            t.commit();
            em.close();
        } catch (Exception e) { 
            t.rollback();
            em.close();
            throw e;
        }
        
        return result;
    }
}
