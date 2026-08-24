package org.duckdns.whocaresleft.repository.mongodb;

import java.util.List;
import java.util.stream.StreamSupport;
import java.util.Collections;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class MongoDoctorRepository implements DoctorRepository {

    private MongoClient client;
    
    public MongoDoctorRepository(MongoClient client) {
        this.client = client;
    }

    @Override
    public List<Doctor> findAll() {
        MongoCollection<Document> doctorCollection = client.getDatabase("hospital").getCollection("doctor");
        return StreamSupport.stream(doctorCollection.find().spliterator(), false)
            .map(d -> Doctor.createDoctor(Id.createId(d.get("id"), d.get("firstName"), d.get("lastName"))))
            .toList(); // Too cumbersome
    }

    @Override
    public void save(Doctor doctor) throws DuplicateDoctorException {
        
    }

    @Override
    public void delete(Id doctorId) throws DoctorNotFoundException {
        
    }

    @Override
    public void update(Id doctorId, Doctor newDoctor) throws DoctorNotFoundException {
        
    }

}
