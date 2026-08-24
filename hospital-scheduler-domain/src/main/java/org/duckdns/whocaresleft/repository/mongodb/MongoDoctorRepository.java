package org.duckdns.whocaresleft.repository.mongodb;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
//import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

public class MongoDoctorRepository implements DoctorRepository {

    private final MongoCollection<Document> doctorCollection;
    
    public MongoDoctorRepository(MongoClient client) {
        doctorCollection = client.getDatabase("hospital").getCollection("doctor");
    }

    @Override
    public List<Doctor> findAll() {
        return StreamSupport.stream(doctorCollection.find().spliterator(), false)
            .map(this::fromDocument)
            .collect(Collectors.toList());
    }

    @Override
    public void save(Doctor doctor) throws DuplicateDoctorException {
        try {
            doctorCollection.insertOne(
                new Document()
                .append("_id", doctor.getId().getValue())
                 .append("firstName", doctor.getFirstName())
                .append("lastName", doctor.getLastName()));
        } catch (MongoWriteException e) {
            throw new DuplicateDoctorException(doctor);
        }
    }

    @Override
    public void delete(Id doctorId) throws DoctorNotFoundException {
        
    }

    @Override
    public void update(Id doctorId, Doctor newDoctor) throws DoctorNotFoundException {
        
    }

    private Doctor fromDocument(Document doc) {
        return Doctor.createDoctor(
            Id.createId(doc.getString("_id")),
            doc.getString("firstName"),
            doc.getString("lastName"));
    }
}
