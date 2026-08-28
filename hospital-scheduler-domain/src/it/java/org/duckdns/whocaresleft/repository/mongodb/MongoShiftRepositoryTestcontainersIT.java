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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.testcontainers.junit.jupiter.Container;

@Testcontainers
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
    
    @BeforeEach
    void setup() {
        client = MongoClients.create(mongo.getConnectionString());
        repository = new MongoShiftRepository(client);
        MongoDatabase database = client.getDatabase("hospital");
        database.drop();
        shiftCollection = database.getCollection("shift");
    }
    
    @AfterEach
    void teardown() {
        client.close();
    }
    
    @Nested @DisplayName("Happy cases")
    class HappyCases {
        
        @Test @DisplayName("FindALl when databse is empty should return empty list")
        void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
            assertThat(repository.findAll())
                .isEmpty();
        }
        
        @Test @DisplayName("FindAll when database is not empty should return all shifts")
        void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllShifts() {
            addTestShiftToDB("doc1", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            addTestShiftToDB("doc2", "er", DATE_24_07_2026, TIME_08_30, TIME_09_30);
            
            assertThat(repository.findAll())
                .containsExactly(
                    Shift.createShift(Id.createId("doc1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30),
                    Shift.createShift(Id.createId("doc2"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_30));
        }
        
        @Test @DisplayName("FindByDoctorId when database is empty should return empty list")
        void testFindByDoctorIdWhenDatabseIsEmptyShouldReturnEmptyList() {
            Id doctorId = Id.createId("doks");
            
            assertThat(repository.findByDoctorId(doctorId))
                .isEmpty();
        }
        
        @Test @DisplayName("FindByDoctorId should only return shifts of that doctor")
        void testFindByDoctorIdShouldOnlyReturnShiftsOfSaidDoctor() {
            addTestShiftToDB("doc1", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            addTestShiftToDB("doc2", "sr", DATE_24_07_2026, TIME_08_00, TIME_09_30);
            addTestShiftToDB("doc1", "sr", DATE_24_07_2026, TIME_08_00, TIME_09_30);
            addTestShiftToDB("doc2", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            
            assertThat(repository.findByDoctorId(Id.createId("doc1")))
                .containsExactly(
                    Shift.createShift(Id.createId("doc1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30),
                    Shift.createShift(Id.createId("doc1"), Id.createId("sr"), DATE_24_07_2026, TIME_08_00, TIME_09_30));
        }
        
        @Test @DisplayName("FindByDepartmentId when database is empty should return empty list")
        void testFindByDepartmentIdWhenDatabseIsEmptyShouldReturnEmptyList() {
            Id departmentId = Id.createId("er");
            
            assertThat(repository.findByDepartmentId(departmentId))
                .isEmpty();
        }
        
        @Test @DisplayName("FindByDepartmentId should only return shifts with that department")
        void testFindByDepartmentIdShouldOnlyReturnShiftsWithSaidDepartment() {
            addTestShiftToDB("doc1", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            addTestShiftToDB("doc2", "sr", DATE_24_07_2026, TIME_08_00, TIME_09_30);
            addTestShiftToDB("doc1", "sr", DATE_24_07_2026, TIME_08_00, TIME_09_30);
            addTestShiftToDB("doc2", "er", DATE_24_07_2026, TIME_08_00, TIME_08_30);
            
            assertThat(repository.findByDepartmentId(Id.createId("er")))
                .containsExactly(
                    Shift.createShift(Id.createId("doc1"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30),
                    Shift.createShift(Id.createId("doc2"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30));
        }
        
        @Test @DisplayName("Save when the exact shift values combination is NOT already present should add normally")
        void testSaveWhenTheExactShiftCombinationIsNotPresentShouldAddToDB() {
            Shift toBeInserted =
                Shift.createShift(
                    Id.createId("doc"),
                    Id.createId("sr"),
                    DATE_24_07_2026,
                    TIME_08_30,
                    TIME_09_00
                );
            
            repository.save(toBeInserted);
            
            assertThat(readAllShiftsFromDB())
                .containsExactly(toBeInserted);
        }
        
        @Test @DisplayName("Delete when the exact shift exists should remove it from database")
        void testDeleteWhenExactShiftExistsShouldRemoveItFromDatabase() {
            addTestShiftToDB("dok", "er", DATE_24_07_2026, TIME_09_00, TIME_09_30);
            addTestShiftToDB("doc", "sr", DATE_24_07_2026, TIME_08_30, TIME_09_00);
            
            repository.delete(Shift.createShift(
                Id.createId("doc"),
                Id.createId("sr"),
                DATE_24_07_2026,
                TIME_08_30,
                TIME_09_00));
            
            assertThat(readAllShiftsFromDB())
                .containsExactly(Shift.createShift(
                    Id.createId("dok"),
                    Id.createId("er"),
                    DATE_24_07_2026,
                    TIME_09_00,
                    TIME_09_30));
        }
        
        @Test @DisplayName("Update when the exact shift exists should update it in database")
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
                
            repository.update(oldDocShift, newDocShift);
            
            assertThat(readAllShiftsFromDB())
                .containsExactly(newDocShift);
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test @DisplayName("Save when the exact shift values combination is already present should throw")
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
                
            assertThatExceptionOfType(OverlappedShiftException.class)
                .isThrownBy(() -> repository.save(alreadyInserted));
            assertThat(readAllShiftsFromDB())
                .contains(alreadyInserted);
        }
        
        @Test @DisplayName("Delete when the exact shift values combination is not present should throw")
        void testDeleteWhenTheExactCombinationIsNotPresentShouldThrow() {
            Shift notPresent = Shift.createShift(
                Id.createId("doc"),
                Id.createId("sr"),
                DATE_24_07_2026,
                TIME_08_30,
                TIME_09_00);
            
            assertThatExceptionOfType(ShiftNotFoundException.class)
                .isThrownBy(() -> repository.delete(notPresent));
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
        shiftCollection.insertOne(toInsert);
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


