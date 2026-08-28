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
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoDepartmentRepository implements DepartmentRepository {
    
    private final MongoCollection<Document> departmentCollection;
    
    public MongoDepartmentRepository(MongoClient client) {
        departmentCollection = client.getDatabase("hospital").getCollection("department");
    }
    
    @Override
    public List<Department> findAll() {
        return StreamSupport.stream(departmentCollection.find().spliterator(), false)
            .map(this::fromDocument)
            .toList();
    }
    
    @Override
    public Department findById(Id departmentId) {
        Document d = departmentCollection.find(Filters.eq("_id", departmentId.getValue())).first();
        if (d == null)
            return null;
        return fromDocument(d);
    }
    
    @Override
    public void save(Department department) throws DuplicateDepartmentException {
        try {
            departmentCollection.insertOne(toDocument(department));
        } catch (MongoWriteException e) {
            throw new DuplicateDepartmentException(department);
        }
    }
    
    @Override
    public void delete(Id departmentId) throws DepartmentNotFoundException {
        DeleteResult result = departmentCollection.deleteOne(Filters.eq("_id", departmentId.getValue()));
        if (result.getDeletedCount() == 0)
            throw new DepartmentNotFoundException(departmentId);
    }
    
    @Override
    public void update(Id departmentId, Department newDepartment) throws DepartmentNotFoundException {
        UpdateResult result = departmentCollection.replaceOne(
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
