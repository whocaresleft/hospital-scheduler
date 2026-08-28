package org.duckdns.whocaresleft.repository.mongodb;

import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.RepositoryProvider;
import org.duckdns.whocaresleft.repository.ShiftRepository;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;

public class MongoRepositoryProvider implements RepositoryProvider {
    
    private static final String DOCTOR_COLLECTION_NAME = "doctor";
    private static final String DEPARTMENT_COLLECTION_NAME = "department";
    private static final String SHIFT_COLLECTION_NAME = "shift";
    
    private final ClientSession mongoClientSession;
    private final MongoDatabase database;
    
    private MongoRepositoryProvider(ClientSession mongoClientSession, MongoDatabase database) {
        this.mongoClientSession = mongoClientSession;
        this.database = database;
    }
    
    @Override
    public DoctorRepository getDoctorRepository() {
        return new MongoDoctorRepository(mongoClientSession, database.getCollection(DOCTOR_COLLECTION_NAME));
    }
    
    @Override
    public DepartmentRepository getDepartmentRepository() {
        return new MongoDepartmentRepository(mongoClientSession, database.getCollection(DEPARTMENT_COLLECTION_NAME));
    }
    
    @Override
    public ShiftRepository getShiftRepository() {
        return new MongoShiftRepository(mongoClientSession, database.getCollection(SHIFT_COLLECTION_NAME));
    }
    
}
