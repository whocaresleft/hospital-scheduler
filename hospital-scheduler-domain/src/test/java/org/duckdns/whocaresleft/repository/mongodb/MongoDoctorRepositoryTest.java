package org.duckdns.whocaresleft.repository.mongodb;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;

import de.bwaldvogel.mongo.MongoServer;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

class MongoDoctorRepositoryTest {

    private static MongoServer server;
    private static InetSocketAddress serverAddress;
    
    private MongoClient client;
    private MongoDoctorRepository repository;
    private MongoCollection<Document> doctorCollection;
    
    @BeforeAll
    static void startMongoServer() {
        server = new MongoServer(new MemoryBackend());
        serverAddress = server.bind();
    }
    
    @AfterAll
    static void shutdownMongoServer() {
        server.shutdown();
    }
    
    @BeforeEach
    void setup() {
        String connectionString = String.format("mongodb://%s:%d",
            serverAddress.getHostName(),
            serverAddress.getPort());
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
    
    @Nested
    class HappyCases {
        @Test
        void testFindAllWhenDatabaseIsEmpty() {
            assertThat(repository.findAll())
                .isEmpty();
        }

        @Test
        void testFindAllWhenDatabaseIsNotEmpty() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            addTestDoctorToDB("doctor_2", "dok", "ter");
            
            assertThat(repository.findAll())
                .containsExactly(
                    Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"),
                    Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
        }
        
        @Test
        void testSaveWhenDoctorIsNotAlreadyInDatabase() {
            Doctor toInsert = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
            repository.save(toInsert);
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(toInsert);
        }
        
        @Test
        void testDeleteWhenDoctorIsPresentInDatabase() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            
            repository.delete(Id.createId("doctor_id"));
            
            assertThat(readAllDoctorsFromDB())
                .doesNotContain(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"));
        }
    }
    
    @Nested
    class ExceptionalCases {
        
        @Test
        void testSaveWhenDoctorIsAlreadyInDatabase() {
            addTestDoctorToDB("doctor_id", "Original", "Doctor");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "A New", "Doctor");
            
            assertThrows(DuplicateDoctorException.class, () -> {
                repository.save(newDoctorWithSameId);
            });
            assertThat(readAllDoctorsFromDB())
                .doesNotContain(newDoctorWithSameId);
        }
        
        @Test
        void testDeleteWhenDoctorIsNotPresentInDatabase() {
            assertThrows(DoctorNotFoundException.class, () -> {
                repository.delete(Id.createId("doctor_id"));
            });
        }
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
            .collect(Collectors.toList());
    }
}
