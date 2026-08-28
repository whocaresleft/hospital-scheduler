package org.duckdns.whocaresleft.repository.mongodb;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.repository.DepartmentRepository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoDepartmentRepository implements DepartmentRepository {
    
    private final ClientSession session;
    private final MongoCollection<Document> departmentCollection;
    
    public MongoDepartmentRepository(ClientSession session, MongoCollection<Document> departmentCollection) {
        this.session = session;
        this.departmentCollection = departmentCollection;
    }
    
    @Override
    public List<Department> findAll() {
        return StreamSupport.stream(departmentCollection.find(session).spliterator(), false)
            .map(this::fromDocument)
            .toList();
    }
    
    @Override
    public Department findById(Id departmentId) {
        Document d = departmentCollection.find(session, Filters.eq("_id", departmentId.getValue())).first();
        if (d == null)
            return null;
        return fromDocument(d);
    }
    
    @Override
    public void save(Department department) throws DuplicateDepartmentException {
        try {
            departmentCollection.insertOne(session, toDocument(department));
        } catch (MongoWriteException e) {
            throw new DuplicateDepartmentException(department);
        }
    }
    
    @Override
    public void delete(Id departmentId) throws DepartmentNotFoundException {
        DeleteResult result = departmentCollection.deleteOne(session, Filters.eq("_id", departmentId.getValue()));
        if (result.getDeletedCount() == 0)
            throw new DepartmentNotFoundException(departmentId);
    }
    
    @Override
    public void update(Id departmentId, Department newDepartment) throws DepartmentNotFoundException {
        UpdateResult result = departmentCollection.replaceOne(
            session,
            Filters.eq("_id", departmentId.getValue()),
            toDocument(newDepartment));
        if (result.getMatchedCount() == 0)
            throw new DepartmentNotFoundException(departmentId);
    }
    
    private Department fromDocument(Document doc) {
        return Department.createDepartment(
            Id.createId(doc.getString("_id")),
            doc.getString("name"));
    }
    
    private Document toDocument(Department dep) {
        return new Document()
            .append("_id", dep.getId().getValue())
            .append("name", dep.getName());
    }
}
