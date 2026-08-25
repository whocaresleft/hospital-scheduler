package org.duckdns.whocaresleft.repository.mongodb;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.testcontainers.junit.jupiter.Container;

@Testcontainers @DisplayName("Integration tests for MongoDoctorRepository using testcontainers")
class MongoDoctorRepositoryTestcontainersIT {
    
    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private MongoClient client;
    private MongoDoctorRepository repository;
    private MongoCollection<Document> doctorCollection;
    
    @BeforeEach
    void setup() {
        String connectionString = String.format("mongodb://%s:%d",
            mongo.getHost(),
            mongo.getFirstMappedPort());
        client = MongoClients.create(connectionString);
        repository = new MongoDoctorRepository(client);
        MongoDatabase database = client.getDatabase("hospital");
        database.drop();
        doctorCollection = database.getCollection("doctor");
    }
    
    @AfterEach
    void teardown() {
        client.close();
    }
    
    @Test
    void testFindAll() {
        addTestDoctorToDB("doctor_1", "doc", "tor");
        addTestDoctorToDB("doctor_2", "dok", "ter");
        
        assertThat(repository.findAll())
            .containsExactly(
                Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"),
                Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
    }
    
    @Test
    void testFindById() {
        addTestDoctorToDB("doctor_1", "doc", "tor");
        addTestDoctorToDB("doctor_2", "dok", "ter");
        
        assertThat(repository.findById(Id.createId("doctor_2")))
            .isEqualTo(Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
    }
    
    @Test
    void testSave() {
        Doctor toBeInserted = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        repository.save(toBeInserted);
        
        assertThat(readAllDoctorsFromDB())
            .containsExactly(toBeInserted);
    }
    
    @Test
    void testDelete() {
        addTestDoctorToDB("doctor_id", "doc", "tor");
        addTestDoctorToDB("doctor_id2", "dok", "ter");
        
        repository.delete(Id.createId("doctor_id"));
        
        assertThat(readAllDoctorsFromDB())
            .containsExactly(Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
    }
    
    @Test 
    void testUpdate() {
        addTestDoctorToDB("doctor_id", "original", "doctor");
        addTestDoctorToDB("doctor_id2", "dok", "ter");
        Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");
        
        repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
        
        assertThat(readAllDoctorsFromDB())
            .containsExactly(
                newDoctorWithSameId,
                Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
    }
    
    private void addTestDoctorToDB(String id, String firstName, String lastName) {
        Document toInsert = new Document()
            .append("_id", id)
            .append("firstName", firstName)
            .append("lastName", lastName);
        doctorCollection.insertOne(toInsert);
    }
    
    private List<Doctor> readAllDoctorsFromDB() {
        return StreamSupport.stream(
            doctorCollection.find().spliterator(), false)
            .map(d -> Doctor.createDoctor(Id.createId(d.getString("_id")), d.getString("firstName"), d.getString("lastName")))
            .toList();
    }
}
