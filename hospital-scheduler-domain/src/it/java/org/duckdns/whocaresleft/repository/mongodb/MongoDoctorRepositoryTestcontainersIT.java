package org.duckdns.whocaresleft.repository.mongodb;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.testcontainers.junit.jupiter.Container;

@Testcontainers @DisplayName("Integration tests for MongoDoctorRepository using Testcontainers")
class MongoDoctorRepositoryTestcontainersIT {
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private MongoClient client;
    private MongoDoctorRepository repository;
    private MongoCollection<Document> doctorCollection;
    private ClientSession session;
    
    @BeforeEach
    void setup() {
        client = MongoClients.create(mongo.getReplicaSetUrl());
        MongoDatabase database = client.getDatabase("hospital");
        database.drop();
        doctorCollection = database.getCollection("doctor");
        session = client.startSession();
        
        repository = new MongoDoctorRepository(session, doctorCollection);
    }
    
    @AfterEach
    void teardown() {
        if (session.hasActiveTransaction())
            session.abortTransaction();
        session.close();
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
        void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllDoctors() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            addTestDoctorToDB("doctor_2", "dok", "ter");
            
            assertThat(repository.findAll())
                .containsExactlyInAnyOrder(
                    Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"),
                    Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
        }
        
        @Test
        void testSaveWhenNoDoctorWithSameIdIsAlreadyInDatabaseShouldAdd() {
            Doctor toBeInserted = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
            
            session.startTransaction();
            repository.save(toBeInserted);
            session.commitTransaction();
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(toBeInserted);
        }
        
        @Test
        void testDeleteWhenDoctorIsPresentInDatabaseShouldDeleteExistingDoctor() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            
            session.startTransaction();
            repository.delete(Id.createId("doctor_id"));
            session.commitTransaction();
            
            assertThat(readAllDoctorsFromDB())
                .isEmpty();
        }
        
        @Test
        void testDeleteWhenDoctorIsPresentInDatabaseAsWellAsAnotherDoctorsShouldDeleteOnlySpecifiedDoctor() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            
            session.startTransaction();
            repository.delete(Id.createId("doctor_id"));
            session.commitTransaction();
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
        }
        
        @Test
        void testUpdateWhenDoctorIsPresentInDatabaseShouldUpdateExistingDoctor() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");
            
            session.startTransaction();
            repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
            session.commitTransaction();
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(newDoctorWithSameId);
        }
        
        @Test
        void testUpdateWhenDoctorIsPresentInDatabaseAsWellAsAnotherDoctorsShouldUpdateOnlySpecifiedDoctor() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");
            
            session.startTransaction();
            repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
            session.commitTransaction();
            
            assertThat(readAllDoctorsFromDB())
                .containsExactlyInAnyOrder(
                    newDoctorWithSameId,
                    Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
        }
        
        @Test
        void testFindByIdWhenDoctorIsPresentInDatabaseShouldReturnSuchDoctor() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            
            assertThat(repository.findById(Id.createId("doctor_id")))
                .isEqualTo(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"));
        }
        
        @Test
        void testFindByIdWhenDoctorIsNotPresentInDatabaseShouldReturnNull() {
            assertThat(repository.findById(Id.createId("doctor_id")))
                .isNull();
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test
        void testSaveWhenDoctorWithSameIdIsAlreadyInDatabaseShouldThrowAndNotSave() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");
            
            session.startTransaction();
            assertThatExceptionOfType(DuplicateDoctorException.class)
                .isThrownBy(() -> repository.save(newDoctorWithSameId));
            session.abortTransaction();
            
            assertThat(readAllDoctorsFromDB())
                .doesNotContain(newDoctorWithSameId);
        }
        
        @Test
        void testDeleteWhenDoctorIsNotPresentInDatabaseShouldThrow() {
            Id validDoctorId = Id.createId("doctor_id");
            
            session.startTransaction();
            assertThatExceptionOfType(DoctorNotFoundException.class)
                .isThrownBy(() -> repository.delete(validDoctorId));
            session.abortTransaction();
        }
        
        @Test
        void testUpdateWhenDoctorIsNotPresentInDatabaseShouldThrow() {
            Id validDoctorId = Id.createId("doctor_id");
            Doctor doctorWithNonExistentId = Doctor.createDoctor(Id.createId("doctor_id"), "a", "doctor");
            
            session.startTransaction();
            assertThatExceptionOfType(DoctorNotFoundException.class)
                .isThrownBy(() -> repository.update(validDoctorId, doctorWithNonExistentId));
            session.abortTransaction();
        }
    }
    
    
    private void addTestDoctorToDB(String id, String firstName, String lastName) {
        Document toInsert = new Document()
            .append("_id", id)
            .append("firstName", firstName)
            .append("lastName", lastName);
        
        session.startTransaction();
        doctorCollection.insertOne(toInsert);
        session.commitTransaction();
    }
    
    private List<Doctor> readAllDoctorsFromDB() {
        return StreamSupport.stream(
            doctorCollection.find().spliterator(), false)
                .map(d -> Doctor.createDoctor(Id.createId(d.getString("_id")), d.getString("firstName"), d.getString("lastName")))
                .toList();
    }
}
