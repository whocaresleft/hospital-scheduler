package org.duckdns.whocaresleft.repository.mongodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.testcontainers.junit.jupiter.Container;

@Testcontainers @DisplayName("Integration tests for MongoDepartmentRepository using Testcontainers")
class MongoDepartmentRepositoryTestcontainersIT {
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private MongoClient client;
    private MongoDepartmentRepository repository;
    private MongoCollection<Document> departmentCollection;
    
    @BeforeEach
    void setup() {
        client = MongoClients.create(mongo.getConnectionString());
        repository = new MongoDepartmentRepository(client);
        MongoDatabase database = client.getDatabase("hospital");
        database.drop();
        departmentCollection = database.getCollection("department");
    }
    
    @AfterEach
    void teardown() {
        client.close();
    }
    
    @Nested @DisplayName("Happy cases")
    class HappyCases {
        
        @Test
        void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
            assertThat(repository.findAll())
                .isEmpty();
        }

        @Test
        void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllDepartments() {
            addTestDepartmentToDB("er", "ER");
            addTestDepartmentToDB("sr", "Surgery Room");
            
            assertThat(repository.findAll())
                .containsExactlyInAnyOrder(
                    Department.createDepartment(Id.createId("er"), "ER"),
                    Department.createDepartment(Id.createId("sr"), "Surgery Room"));
        }
        
        @Test
        void testSaveWhenNoDepartmentWithSameIdIsAlreadyInDatabaseShouldAdd() {
            Department toBeInserted = Department.createDepartment(Id.createId("er"), "ER");
            
            repository.save(toBeInserted);
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(toBeInserted);
        }
        
        @Test
        void testDeleteWhenDepartmentIsPresentInDatabaseShouldDeleteExistingDepartment() {
            addTestDepartmentToDB("er", "ER");
            
            repository.delete(Id.createId("er"));
            
            assertThat(readAllDepartmentsFromDB())
                .isEmpty();
        }
        
        @Test
        void testDeleteWhenDepartmentIsPresentInDatabaseAsWellAsAnotherDepartmentsShouldDeleteOnlySpecifiedDepartment() {
            addTestDepartmentToDB("er", "ER");
            addTestDepartmentToDB("sr", "Surgery Room");
            
            repository.delete(Id.createId("er"));
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(Department.createDepartment(Id.createId("sr"), "Surgery Room"));
        }
        
        @Test
        void testUpdateWhenDepartmentIsPresentInDatabaseShouldUpdateExistingDepartment() {
            addTestDepartmentToDB("er", "ER");
            Department newDepartmentWithSameId = Department.createDepartment(Id.createId("er"), "Newly Improved ER");
            
            repository.update(Id.createId("er"), newDepartmentWithSameId);
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(newDepartmentWithSameId);
        }
        
        @Test
        void testUpdateWhenDepartmentIsPresentInDatabaseAsWellAsAnotherDepartmentsShouldUpdateOnlySpecifiedDepartment() {
            addTestDepartmentToDB("er", "ER");
            addTestDepartmentToDB("sr", "Surgery Room");
            Department newDepartmentWithSameId = Department.createDepartment(Id.createId("er"), "Newly Improved ER");

            repository.update(Id.createId("er"), newDepartmentWithSameId);
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactlyInAnyOrder(
                    newDepartmentWithSameId,
                    Department.createDepartment(Id.createId("sr"), "Surgery Room"));
        }
        
        @Test
        void testFindByIdWhenDepartmentIsPresentInDatabaseShouldReturnSuchDepartment() {
            addTestDepartmentToDB("er", "ER");
            addTestDepartmentToDB("sr", "Surgery Room");
            
            assertThat(repository.findById(Id.createId("sr")))
                .isEqualTo(Department.createDepartment(Id.createId("sr"), "Surgery Room"));
        }
        
        @Test
        void testFindByIdWhenDepartmentIsNotPresentInDatabaseShouldReturnNull() {
            assertThat(repository.findById(Id.createId("sr")))
                .isNull();
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test
        void testSaveWhenDepartmentWithSameIdIsAlreadyInDatabaseShouldThrowAndNotSave() {
            addTestDepartmentToDB("er", "ER");
            Department newDepartmentWithSameId = Department.createDepartment(Id.createId("er"), "Newly Improved ER");
            
            assertThatExceptionOfType(DuplicateDepartmentException.class)
                .isThrownBy(() -> repository.save(newDepartmentWithSameId));
            assertThat(readAllDepartmentsFromDB())
                .doesNotContain(newDepartmentWithSameId);
        }
        
        @Test
        void testDeleteWhenDepartmentIsNotPresentInDatabaseShouldThrow() {
            Id nonExistentDepartmentId = Id.createId("er");
            
            assertThatExceptionOfType(DepartmentNotFoundException.class)
                .isThrownBy(() -> repository.delete(nonExistentDepartmentId));
        }
        
        @Test
        void testUpdateWhenDepartmentIsNotPresentInDatabaseShouldThrow() {
            Id nonExistentDepartmentId = Id.createId("er");
            Department departmentWithNonExistentId = Department.createDepartment(nonExistentDepartmentId, "New ER");
            
            assertThatExceptionOfType(DepartmentNotFoundException.class)
                .isThrownBy(() -> repository.update(nonExistentDepartmentId, departmentWithNonExistentId));
        }
    }
    
    private void addTestDepartmentToDB(String id, String name) {
        Document toInsert = new Document()
            .append("_id", id)
            .append("name", name);
        departmentCollection.insertOne(toInsert);
    }
    
    private List<Department> readAllDepartmentsFromDB() {
        return StreamSupport.stream(
            departmentCollection.find().spliterator(), false)
                .map(d -> Department.createDepartment(Id.createId(d.getString("_id")), d.getString("name")))
                .toList();
    }
}
