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
        
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
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
