package org.duckdns.whocaresleft.repository.mongodb;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoDoctorRepository implements DoctorRepository {
    
    private final ClientSession session;
    private final MongoCollection<Document> doctorCollection;
    
    public MongoDoctorRepository(ClientSession session, MongoCollection<Document> doctorCollection) {
        this.session = session;
        this.doctorCollection = doctorCollection;
    }
    
    @Override
    public List<Doctor> findAll() {
        return StreamSupport.stream(doctorCollection.find(session).spliterator(), false)
            .map(this::fromDocument)
            .toList();
    }
    
    @Override
    public Doctor findById(Id doctorId) {
        Document d = doctorCollection.find(session, Filters.eq("_id", doctorId.getValue())).first();
        if (d == null)
            return null;
        return fromDocument(d);
    }
    
    @Override
    public void save(Doctor doctor) throws DuplicateDoctorException {
        Doctor found = findById(doctor.getId());
        if (found != null) {
            throw new DuplicateDoctorException(found);
        }
        doctorCollection.insertOne(session, toDocument(doctor));
    }
    
    @Override
    public void delete(Id doctorId) throws DoctorNotFoundException {
        DeleteResult result = doctorCollection.deleteOne(session, Filters.eq("_id", doctorId.getValue()));
        if (result.getDeletedCount() == 0)
            throw new DoctorNotFoundException(doctorId);
    }
    
    @Override
    public void update(Id doctorId, Doctor newDoctor) throws DoctorNotFoundException {
        UpdateResult result = doctorCollection.replaceOne(
            session,
            Filters.eq("_id", doctorId.getValue()),
            toDocument(newDoctor));
        if (result.getMatchedCount() == 0)
            throw new DoctorNotFoundException(doctorId);
    }
    
    private Doctor fromDocument(Document doc) {
        return Doctor.createDoctor(
            Id.createId(doc.getString("_id")),
            doc.getString("firstName"),
            doc.getString("lastName"));
    }
    
    private Document toDocument(Doctor doc) {
        return new Document()
            .append("_id", doc.getId().getValue())
            .append("firstName", doc.getFirstName())
            .append("lastName", doc.getLastName());
    }
}
