package org.duckdns.whocaresleft.repository.mongodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.OverlappedShiftException;
import org.duckdns.whocaresleft.exception.ShiftNotFoundException;
import org.duckdns.whocaresleft.model.Shift;
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

@Testcontainers  @DisplayName("Integration tests for MongoShiftRepository using Testcontainers")
class MongoShiftRepositoryTestcontainersIT {
    
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    private static final LocalTime TIME_08_30 = LocalTime.of(8, 30);
    private static final LocalTime TIME_09_30 = LocalTime.of(9, 30);
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    private MongoClient client;
    private MongoShiftRepository repository;
    private MongoCollection<Document> shiftCollection;
    private ClientSession session;
    
    @BeforeEach
    void setup() {
        client = MongoClients.create(mongo.getReplicaSetUrl());
        MongoDatabase database = client.getDatabase("hospital");
        database.drop();
        shiftCollection = database.getCollection("shift");
        session = client.startSession();
        
        repository = new MongoShiftRepository(session, shiftCollection);
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
        void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllShifts() {
            addTestShiftToDB("doc1", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            addTestShiftToDB("doc2", "er", DATE_24_07_2026, TIME_08_30, TIME_09_30);
            
            assertThat(repository.findAll())
                .containsExactlyInAnyOrder(
                    Shift.createShift(Id.createId("doc1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30),
                    Shift.createShift(Id.createId("doc2"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_30));
        }
        
        @Test
        void testFindByDoctorIdWhenDatabseIsEmptyShouldReturnEmptyList() {
            Id doctorId = Id.createId("doks");
            
            assertThat(repository.findByDoctorId(doctorId))
                .isEmpty();
        }
        
        @Test
        void testFindByDoctorIdShouldOnlyReturnShiftsOfSaidDoctor() {
            addTestShiftToDB("doc1", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            addTestShiftToDB("doc2", "sr", DATE_24_07_2026, TIME_08_00, TIME_09_30);
            addTestShiftToDB("doc1", "sr", DATE_24_07_2026, TIME_08_00, TIME_09_30);
            addTestShiftToDB("doc2", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            
            assertThat(repository.findByDoctorId(Id.createId("doc1")))
                .containsExactlyInAnyOrder(
                    Shift.createShift(Id.createId("doc1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30),
                    Shift.createShift(Id.createId("doc1"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_30));
        }
        
        @Test
        void testFindByDepartmentIdWhenDatabseIsEmptyShouldReturnEmptyList() {
            Id departmentId = Id.createId("er");
            
            assertThat(repository.findByDepartmentId(departmentId))
                .isEmpty();
        }
        
        @Test
        void testFindByDepartmentIdShouldOnlyReturnShiftsWithSaidDepartment() {
            addTestShiftToDB("doc1", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            addTestShiftToDB("doc2", "sr", DATE_24_07_2026, TIME_08_00, TIME_09_30);
            addTestShiftToDB("doc1", "sr", DATE_24_07_2026, TIME_08_00, TIME_09_30);
            addTestShiftToDB("doc2", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            
            assertThat(repository.findByDepartmentId(Id.createId("er")))
                .containsExactlyInAnyOrder(
                    Shift.createShift(Id.createId("doc1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30),
                    Shift.createShift(Id.createId("doc2"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30));
        }
        
        @Test
        void testSaveWhenTheExactShiftCombinationIsNotPresentShouldAddToDB() {
            Shift toBeInserted =
                Shift.createShift(
                    Id.createId("doc"),
                    Id.createId("sr"),
                    DATE_24_07_2026,
                    TIME_08_30,
                    TIME_09_00
                );
            
            session.startTransaction();
            repository.save(toBeInserted);
            session.commitTransaction();
            
            assertThat(readAllShiftsFromDB())
                .containsExactly(toBeInserted);
        }
        
        @Test
        void testDeleteWhenExactShiftExistsShouldRemoveItFromDatabase() {
            addTestShiftToDB("dok", "er", DATE_24_07_2026, TIME_09_00, TIME_09_30);
            addTestShiftToDB("doc", "sr", DATE_24_07_2026, TIME_08_30, TIME_09_00);
            
            session.startTransaction();
            repository.delete(Shift.createShift(
                Id.createId("doc"),
                Id.createId("sr"),
                DATE_24_07_2026,
                TIME_08_30,
                TIME_09_00));
            session.commitTransaction();
            
            assertThat(readAllShiftsFromDB())
                .containsExactlyInAnyOrder(Shift.createShift(
                    Id.createId("dok"),
                    Id.createId("er"),
                    DATE_24_07_2026,
                    TIME_09_00,
                    TIME_09_30));
        }
        
        @Test
        void testUpdateWhenExactShiftExistsShouldUpdateItInDatabase() {
            addTestShiftToDB("doc", "sr", DATE_24_07_2026, TIME_08_30, TIME_09_00);
            
            Shift oldDocShift
                = Shift.createShift(
                    Id.createId("doc"),
                    Id.createId("sr"),
                    DATE_24_07_2026,
                    TIME_08_30,
                    TIME_09_00);
            Shift newDocShift
                = Shift.createShift(
                    Id.createId("doc"),
                    Id.createId("playground"),
                    DATE_24_07_2026,
                    TIME_09_00,
                    TIME_09_30);
                
            session.startTransaction();
            repository.update(oldDocShift, newDocShift);
            session.commitTransaction();
            
            assertThat(readAllShiftsFromDB())
                .containsExactly(newDocShift);
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test
        void testSaveWhenTheExactCombinationIsPresentShouldThrow() {
            addTestShiftToDB("doc", "sr", DATE_24_07_2026, TIME_08_30, TIME_09_00);
            
            Shift alreadyInserted =
                    Shift.createShift(
                        Id.createId("doc"),
                        Id.createId("sr"),
                        DATE_24_07_2026,
                        TIME_08_30,
                        TIME_09_00
                    );
                
            session.startTransaction();
            assertThatExceptionOfType(OverlappedShiftException.class)
                .isThrownBy(() -> repository.save(alreadyInserted));
            session.abortTransaction();
            
            assertThat(readAllShiftsFromDB())
                .contains(alreadyInserted);
        }
        
        @Test
        void testDeleteWhenTheExactCombinationIsNotPresentShouldThrow() {
            Shift notPresent = Shift.createShift(
                Id.createId("doc"),
                Id.createId("sr"),
                DATE_24_07_2026,
                TIME_08_30,
                TIME_09_00);
            
            session.startTransaction();
            assertThatExceptionOfType(ShiftNotFoundException.class)
                .isThrownBy(() -> repository.delete(notPresent));
            session.abortTransaction();
        }
    }
    
    private void addTestShiftToDB(String doctorId, String departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String documentId = String.format("%s-%s-%s-%s-%s",
            doctorId,
            departmentId,
            date.toString(),
            startTime.toString(),
            endTime.toString());
        
        Document toInsert = new Document()
            .append("_id", documentId)
            .append("doctorId", doctorId)
            .append("departmentId", departmentId)
            .append("date", date.toString())
            .append("startTime", startTime.toString())
            .append("endTime", endTime.toString());
        
        session.startTransaction();
        shiftCollection.insertOne(toInsert);
        session.commitTransaction();
    }
    
    private List<Shift> readAllShiftsFromDB() {
        return StreamSupport.stream(
            shiftCollection.find().spliterator(), false)
                .map(s -> {
                    return Shift.createShift(
                        Id.createId(s.getString("doctorId")),
                        Id.createId(s.getString("departmentId")),
                        LocalDate.parse(s.getString("date")),
                        LocalTime.parse(s.getString("startTime")),
                        LocalTime.parse(s.getString("endTime")));
                })
                .toList();
    }
}


