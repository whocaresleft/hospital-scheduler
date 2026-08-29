package org.duckdns.whocaresleft.transaction.mongodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Testcontainers @DisplayName("Integration tests for MongoTransactionManager using Testcontainers")
class MongoTransactionManagerTestcontainersIT {
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private MongoClient client;
    private MongoCollection<Document> departmentCollection;
    
    private MongoTransactionManager transactionManager;
    
    @BeforeEach
    void setup() {
        client = MongoClients.create(mongo.getReplicaSetUrl());
        MongoDatabase database = client.getDatabase("hospital");
        database.drop();
        departmentCollection = database.getCollection("department");
        
        transactionManager = new MongoTransactionManager(client, database);
    }
    
    @AfterEach
    void teardown() {
        client.close();
    }
    
    @Nested @DisplayName("Happy cases")
    class HappyCases {
        
        @Test
        void testTransactionThatDoesNotProduceErrorsIsCorrectlyRegisteredToDB() {
            Department toAdd = Department.createDepartment(Id.createId("er"), "ER");
            
            transactionManager.doInTransaction(repositoryProvider -> {
                repositoryProvider.getDepartmentRepository().save(toAdd);
                return null;
            });
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(toAdd);
        }
        
        @Test
        void testTransactionWithMultipleOperationsThatSucceeds() {
            Department toAdd = Department.createDepartment(Id.createId("er"), "ER");
            Department anotherToAdd = Department.createDepartment(Id.createId("sr"), "Surgery Room");
            
            transactionManager.doInTransaction(repositoryProvider -> {
                repositoryProvider.getDepartmentRepository().save(toAdd);
                repositoryProvider.getDepartmentRepository().save(anotherToAdd);
                return null;
            });
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactlyInAnyOrder(toAdd, anotherToAdd);
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test
        void testTransactionThatProducesExceptionIsAbortedNotRegisteredToDBAndForwardsTheException() {
            addTestDepartmentToDB("er", "Old Emergency Room");
            Department toAdd = Department.createDepartment(Id.createId("er"), "New Emergency Room");
            
            assertThatExceptionOfType(DuplicateDepartmentException.class)
                .isThrownBy(() -> 
                    transactionManager.doInTransaction(repositoryProvider -> {
                        repositoryProvider.getDepartmentRepository().save(toAdd);
                        return null;
                }));
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(Department.createDepartment(Id.createId("er"), "Old Emergency Room"));
        }
        
        @Test
        void testTransactionWithMultipleOperationsThatProducesExceptionIsAbortedNotRegisteredToDBAndForwardsTheException() {
            addTestDepartmentToDB("er", "Old Emergency Room");
            Department toAddCorrect = Department.createDepartment(Id.createId("sr"), "Surgery Room");
            Department toAddConflicting = Department.createDepartment(Id.createId("er"), "New Emergency Room");
            
            assertThatExceptionOfType(DuplicateDepartmentException.class)
                .isThrownBy(() -> 
                    transactionManager.doInTransaction(repositoryProvider -> {
                        repositoryProvider.getDepartmentRepository().save(toAddCorrect);
                        repositoryProvider.getDepartmentRepository().save(toAddConflicting);
                        return null;
                }));
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(Department.createDepartment(Id.createId("er"), "Old Emergency Room"));
        }
    }
    
    private void addTestDepartmentToDB(String id, String name) {
        departmentCollection.insertOne(
            new Document()
                .append("_id", id)
                .append("name", name));
    }
    
    private List<Department> readAllDepartmentsFromDB() {
        return StreamSupport.stream(departmentCollection.find().spliterator(), false)
            .map(d ->
                Department.createDepartment(Id.createId(d.getString("_id")), d.getString("name")))
            .toList();
    }
}
