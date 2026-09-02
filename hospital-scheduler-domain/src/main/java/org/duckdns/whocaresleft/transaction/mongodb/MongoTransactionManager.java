package org.duckdns.whocaresleft.transaction.mongodb;

import org.duckdns.whocaresleft.repository.RepositoryProvider;
import org.duckdns.whocaresleft.repository.mongodb.MongoRepositoryProvider;
import org.duckdns.whocaresleft.transaction.TransactionCode;
import org.duckdns.whocaresleft.transaction.TransactionManager;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoTransactionManager implements TransactionManager {
    
    private final MongoClient client;
    private final MongoDatabase database;
    
    private final String doctorCollectionName;
    private final String departmentCollectionName;
    private final String shiftCollectionName;
    
    public MongoTransactionManager(MongoClient client, MongoDatabase database,
            String doctorCollectionName, String departmentCollectionName, String shiftCollectionName)
    {
        this.client = client;
        this.database = database;
        
        this.doctorCollectionName = doctorCollectionName;
        this.departmentCollectionName = departmentCollectionName;
        this.shiftCollectionName = shiftCollectionName;
    }
    
    @Override
    public <T> T doInTransaction(TransactionCode<T> code) {
        ClientSession session = client.startSession();
        RepositoryProvider provider = new MongoRepositoryProvider(session, database, doctorCollectionName, departmentCollectionName, shiftCollectionName);
        return session.withTransaction(() ->  code.apply(provider) );
    }

}
