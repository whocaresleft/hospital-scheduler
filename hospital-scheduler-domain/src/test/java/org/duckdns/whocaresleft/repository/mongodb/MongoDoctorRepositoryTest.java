package org.duckdns.whocaresleft.repository.mongodb;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.InetSocketAddress;
import java.util.List;
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

@DisplayName("Unit tests for MongoDoctorRepository")
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
    
    @Nested @DisplayName("Happy cases")
    class HappyCases {
        
        @Test @DisplayName("FindAll when database is empty should return empty list")
        void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
            assertThat(repository.findAll())
                .isEmpty();
        }

        @Test @DisplayName("FindAll when database is not empty should return all the doctors")
        void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllDoctors() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            addTestDoctorToDB("doctor_2", "dok", "ter");
            
            assertThat(repository.findAll())
                .containsExactly(
                    Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"),
                    Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
        }
        
        @Test @DisplayName("Save when the no doctor with the same is already in the database should add")
        void testSaveWhenNoDoctorWithSameIdIsAlreadyInDatabaseShouldAdd() {
            Doctor toBeInserted = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
            repository.save(toBeInserted);
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(toBeInserted);
        }
        
        @Test @DisplayName("Delete when a doctor with the specified id is present in the database should delete existing doctor")
        void testDeleteWhenDoctorIsPresentInDatabaseShouldDeleteExistingDoctor() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            
            repository.delete(Id.createId("doctor_id"));
            
            assertThat(readAllDoctorsFromDB())
                .doesNotContain(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"))
                .isEmpty();
        }
        
        @Test @DisplayName("Delete when the doctor is present in the database, as well as other doctors, should only delete the specified one")
        void testDeleteWhenDoctorIsPresentInDatabaseAsWellAsAnotherDoctorsShouldDeleteOnlySpecifiedDoctor() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            
            repository.delete(Id.createId("doctor_id"));
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
        }
        
        @Test @DisplayName("Update when a doctor with the specified id is present in the database should update the existing doctor")
        void testUpdateWhenDoctorIsPresentInDatabaseShouldUpdateExistingDoctor() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");
            
            repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(newDoctorWithSameId);
        }
        
        @Test @DisplayName("Update when the doctor is present in the database, as well as other doctors, should only update the specified one")
        void testUpdateWhenDoctorIsPresentInDatabaseAsWellAsAnotherDoctorsShouldDeleteOnlySpecifiedDoctor() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");
            
            repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(
                    newDoctorWithSameId,
                    Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
        }
        
        @Test @DisplayName("FindById when the doctor is present in the database should return the doctor with that id")
        void testFindByIdWhenDoctorIsPresentInDatabaseShouldReturnSuchDoctor() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            
            assertThat(repository.findById(Id.createId("doctor_id")))
                .isEqualTo(Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor"));
        }
        
        @Test @DisplayName("FindById when the doctor is not present in the database should return null")
        void testFindByIdWhenDoctorIsNotPresentInDatabaseShouldReturnNull() {
            assertThat(repository.findById(Id.createId("doctor_id")))
                .isEqualTo(null);
        }
    }
    
    @Nested
    class ExceptionalCases {
        
        @Test @DisplayName("Save when a doctor with the sane id is already present in the database should throw and not add")
        void testSaveWhenDoctorWithSameIdIsAlreadyInDatabaseShouldThrowAndNotSave() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");
            
            assertThatExceptionOfType(DuplicateDoctorException.class)
                .isThrownBy(() -> repository.save(newDoctorWithSameId));
            assertThat(readAllDoctorsFromDB())
                .doesNotContain(newDoctorWithSameId);
        }
        
        @Test @DisplayName("Delete when no doctor with the specified id is in the database should throw")
        void testDeleteWhenDoctorIsNotPresentInDatabaseShouldThrow() {
            Id validDoctorId = Id.createId("doctor_id");
            assertThatExceptionOfType(DoctorNotFoundException.class)
                .isThrownBy(() -> repository.delete(validDoctorId));
        }
        
        @Test @DisplayName("Update when no doctor with the specified id is in the database should throw")
        void testUpdateWhenDoctorIsNotPresentInDatabaseShouldThrow() {
            Id validDoctorId = Id.createId("doctor_id");
            Doctor doctorWithNonExistentId = Doctor.createDoctor(Id.createId("doctor_id"), "a", "doctor");
            
            assertThatExceptionOfType(DoctorNotFoundException.class)
                .isThrownBy(() -> repository.update(validDoctorId, doctorWithNonExistentId));
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
            .toList();
    }
}
