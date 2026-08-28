package org.duckdns.whocaresleft.repository.mariadb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.ShiftNotFoundException;
import org.duckdns.whocaresleft.exception.OverlappedShiftException;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.repository.mariadb.entity.ShiftEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers @DisplayName("Integration tests for MariaShiftRepository using Testcontainers")
class MariaShiftDepartmentRepositoryTestcontainersIT {
    
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    private static final LocalTime TIME_08_30 = LocalTime.of(8, 30);
    private static final LocalTime TIME_09_30 = LocalTime.of(9, 30);
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    private static EntityManagerFactory emf;
    
    private EntityManager entityManager;
    private MariaShiftRepository repository;
    
    @BeforeAll
    static void setupEntityManagerFactory() {
        Map<String, String> properties = Map.of(
            "jakarta.persistence.jdbc.url", maria.getJdbcUrl(),
            "jakarta.persistence.jdbc.user", maria.getUsername(),
            "jakarta.persistence.jdbc.password", maria.getPassword(),
            "jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver",
            "hibernate.hbm2ddl.auto", "create-drop");
        emf = Persistence.createEntityManagerFactory("maria_repository_it", properties);
    }
    
    @AfterAll
    static void teardownEntityManagerFactory() {
        if (emf != null)
            emf.close();
    }
    
    @BeforeEach
    void setup() {
        entityManager = emf.createEntityManager();
        
        repository = new MariaShiftRepository(entityManager);
        
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM ShiftEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM DoctorEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM DepartmentEntity").executeUpdate();
        entityManager.getTransaction().commit();
    }
    
    @AfterEach
    void teardown() {
        if (entityManager.isOpen()) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            entityManager.close();
        }
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
        void testFindByDoctorIdWhenDatabaseIsEmptyShouldReturnEmptyList() {
            Id doctorId = Id.createId("doctor_id");
            
            assertThat(repository.findByDoctorId(doctorId))
                .isEmpty();
        }
        
        @Test
        void testFindByDoctorIdWhenDatabaseIsNotEmptyShouldReturnShiftsOfSaidDoctor() {
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
        void testFindByDepartmentIdWhenDatabaseIsEmptyShouldReturnEmptyList() {
            Id departmentId = Id.createId("department_id");
            
            assertThat(repository.findByDepartmentId(departmentId))
                .isEmpty();
        }
        
        @Test
        void testFindByDepartmentIdShouldOnlyReturnShiftWithSaidDepartment() {
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
        void testSaveWhenTheExactShiftCombinationISNotPresentShouldAddToDB() {
            Shift toBeInserted =
                Shift.createShift(
                    Id.createId("doc"),
                    Id.createId("sr"),
                    DATE_24_07_2026,
                    TIME_08_30,
                    TIME_09_00
                );
            
            entityManager.getTransaction().begin();
            repository.save(toBeInserted);
            entityManager.getTransaction().commit();
            
            assertThat(readAllShiftsFromDB())
                .containsExactly(toBeInserted);
        }
        
        @Test
        void testDeleteWhenTheExactCombinationIsPresentShouldRemoveFromDB() {
            addTestShiftToDB("dok", "er", DATE_24_07_2026, TIME_09_00, TIME_09_30);
            addTestShiftToDB("doc", "sr", DATE_24_07_2026, TIME_08_30, TIME_09_00);
            
            entityManager.getTransaction().begin();
            repository.delete(Shift.createShift(
                Id.createId("doc"),
                Id.createId("sr"),
                DATE_24_07_2026,
                TIME_08_30,
                TIME_09_00));
            entityManager.getTransaction().commit();
            
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
            
            entityManager.getTransaction().begin();
            repository.update(oldDocShift, newDocShift);
            entityManager.getTransaction().commit();
            
            assertThat(readAllShiftsFromDB())
                .containsExactly(newDocShift);
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test
        void testSaveWhenTheExactCombinationIsPresendShouldThrow() {
            addTestShiftToDB("doc", "sr", DATE_24_07_2026, TIME_08_30, TIME_09_00);
            
            Shift alreadyInserted =
                Shift.createShift(
                    Id.createId("doc"),
                    Id.createId("sr"),
                    DATE_24_07_2026,
                    TIME_08_30,
                    TIME_09_00
                );
            
            entityManager.getTransaction().begin();
            assertThatExceptionOfType(OverlappedShiftException.class)
                .isThrownBy(() -> repository.save(alreadyInserted));
            entityManager.getTransaction().rollback();
            
            assertThat(readAllShiftsFromDB())
                .contains(alreadyInserted);
        }
        
        @Test
        void testDeleteWhenTheExactCombinationIsNotPresendShouldThrow() {
            Shift notPresent = Shift.createShift(
                Id.createId("doc"),
                Id.createId("sr"),
                DATE_24_07_2026,
                TIME_08_30,
                TIME_09_00);

            entityManager.getTransaction().begin();
            assertThatExceptionOfType(ShiftNotFoundException.class)
                .isThrownBy(() -> repository.delete(notPresent));
            entityManager.getTransaction().rollback();
        }
    }
    
    private void addTestShiftToDB(String doctorId, String departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String shiftId = String.format("%s-%s-%s-%s-%s",
            doctorId,
            departmentId,
            date.toString(),
            startTime.toString(),
            endTime.toString());
        
        entityManager.getTransaction().begin();
        entityManager.persist(
            new ShiftEntity(
                shiftId,
                doctorId,
                departmentId,
                date,
                startTime,
                endTime));
        entityManager.getTransaction().commit();
        entityManager.clear();
    }
    
    private List<Shift> readAllShiftsFromDB() {
        entityManager.clear();
        return entityManager.createQuery("SELECT e FROM ShiftEntity e", ShiftEntity.class)
            .getResultStream()
            .map(ShiftEntity::toShift)
            .toList();
    }
}
