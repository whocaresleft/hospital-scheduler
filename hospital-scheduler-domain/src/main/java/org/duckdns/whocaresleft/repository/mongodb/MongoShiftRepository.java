package org.duckdns.whocaresleft.repository.mongodb;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.OverlappedShiftException;
import org.duckdns.whocaresleft.exception.ShiftNotFoundException;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.repository.ShiftRepository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

public class MongoShiftRepository implements ShiftRepository {
    
    private static final String DOCTOR_ID_FIELD_NAME = "doctorId";
    private static final String DEPARTMENT_ID_FIELD_NAME = "departmentId";
    private final MongoCollection<Document> shiftCollection;
    
    public MongoShiftRepository(MongoClient client) {
        shiftCollection = client.getDatabase("hospital").getCollection("shift");
    }
    
    @Override
    public List<Shift> findAll() {
        return StreamSupport.stream(shiftCollection.find().spliterator(), false)
            .map(this::fromDocument)
            .toList();
    }
    
    @Override
    public List<Shift> findByDoctorId(Id doctorId) {
        return StreamSupport.stream(shiftCollection.find(Filters.eq(DOCTOR_ID_FIELD_NAME, doctorId.getValue())).spliterator(), false)
            .map(this::fromDocument)
            .toList();
    }
    
    @Override
    public List<Shift> findByDepartmentId(Id departmentId) {
        return StreamSupport.stream(shiftCollection.find(Filters.eq(DEPARTMENT_ID_FIELD_NAME, departmentId.getValue())).spliterator(), false)
            .map(this::fromDocument)
            .toList();
    }
    
    @Override
    public void save(Shift shift) throws OverlappedShiftException {
        try {
            shiftCollection.insertOne(toDocument(shift));
        } catch (MongoWriteException e) {
            throw new OverlappedShiftException(shift, shift);
        }
    }
    
    @Override
    public void delete(Shift shift) throws ShiftNotFoundException {
        DeleteResult result = shiftCollection.deleteOne(Filters.eq("_id", generateDocumentId(shift)));
        if (result.getDeletedCount() == 0)
            throw new ShiftNotFoundException(shift);
    }
    
    @Override
    public void update(Shift oldShift, Shift newShift) throws ShiftNotFoundException, OverlappedShiftException {
        delete(oldShift);
        save(newShift);
    }
    
    private Shift fromDocument(Document doc) {
        return Shift.createShift(
            Id.createId(doc.getString(DOCTOR_ID_FIELD_NAME)),
            Id.createId(doc.getString(DEPARTMENT_ID_FIELD_NAME)),
            LocalDate.parse(doc.getString("date")),
            LocalTime.parse(doc.getString("startTime")),
            LocalTime.parse(doc.getString("endTime")));
    }
    
    private Document toDocument(Shift sh) {
        return new Document()
            .append("_id", generateDocumentId(sh))
            .append(DOCTOR_ID_FIELD_NAME, sh.getDoctorId().getValue())
            .append(DEPARTMENT_ID_FIELD_NAME, sh.getDepartmentId().getValue())
            .append("date", sh.getDate().toString())
            .append("startTime", sh.getStartTime().toString())
            .append("endTime", sh.getEndTime().toString());
    }
    
    private static String generateDocumentId(Shift sh) {
        return String.format("%s-%s-%s-%s-%s",
            sh.getDoctorId().getValue(),
            sh.getDepartmentId().getValue(),
            sh.getDate().toString(),
            sh.getStartTime().toString(),
            sh.getEndTime().toString());
    }
}
