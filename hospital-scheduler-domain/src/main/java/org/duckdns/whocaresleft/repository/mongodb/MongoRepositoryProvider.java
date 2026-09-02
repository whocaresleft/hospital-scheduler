package org.duckdns.whocaresleft.repository.mongodb;

import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.RepositoryProvider;
import org.duckdns.whocaresleft.repository.ShiftRepository;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;

public class MongoRepositoryProvider implements RepositoryProvider {
    
    private final ClientSession mongoClientSession;
    private final MongoDatabase database;
    
    private final String doctorCollectionName;
    private final String departmentCollectionName;
    private final String shiftCollectionName;
    
    public MongoRepositoryProvider(ClientSession mongoClientSession, MongoDatabase database,
        String doctorCollectionName, String departmentCollectionName, String shiftCollectionName)
    {
        this.mongoClientSession = mongoClientSession;
        this.database = database;
        
        this.doctorCollectionName = doctorCollectionName;
        this.departmentCollectionName = departmentCollectionName;
        this.shiftCollectionName = shiftCollectionName;
    }
    
    @Override
    public DoctorRepository getDoctorRepository() {
        return new MongoDoctorRepository(mongoClientSession, database.getCollection(doctorCollectionName));
    }
    
    @Override
    public DepartmentRepository getDepartmentRepository() {
        return new MongoDepartmentRepository(mongoClientSession, database.getCollection(departmentCollectionName));
    }
    
    @Override
    public ShiftRepository getShiftRepository() {
        return new MongoShiftRepository(mongoClientSession, database.getCollection(shiftCollectionName));
    }
    
}
