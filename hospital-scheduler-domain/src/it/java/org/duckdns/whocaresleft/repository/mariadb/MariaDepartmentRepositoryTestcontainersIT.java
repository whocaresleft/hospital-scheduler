package org.duckdns.whocaresleft.repository.mariadb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.repository.mariadb.entity.DepartmentEntity;
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

@Testcontainers @DisplayName("Integration tests for MariaDepartmentRepository using Testcontainers")
class MariaDepartmentRepositoryTestcontainersIT {
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    private static EntityManagerFactory emf;
    
    private EntityManager entityManager;
    private MariaDepartmentRepository repository;
    
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
        
        repository = new MariaDepartmentRepository(entityManager);
        
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
        void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllDepartments() {
            addTestDepartmentToDB("er", "Emergency Room");
            addTestDepartmentToDB("sr", "Surgery Room");
            
            assertThat(repository.findAll())
                .containsExactlyInAnyOrder(
                    Department.createDepartment(Id.createId("er"), "Emergency Room"),
                    Department.createDepartment(Id.createId("sr"), "Surgery Room"));
        }
        
        @Test
        void testFindByIdWhenDepartmentIsNotPresentInDatabaseShouldReturnNull() {
            addTestDepartmentToDB("er", "Emergency Room");
            
            assertThat(repository.findById(Id.createId("does_not_exist")))
                .isNull();
        }
        
        @Test
        void testFindByIdWhenDepartmentIsPresentInDatabaseShouldReturnIt() {
            addTestDepartmentToDB("er", "Emergency Room");
            addTestDepartmentToDB("sr", "Surgery Room");
            
            assertThat(repository.findById(Id.createId("er")))
                .isEqualTo(Department.createDepartment(Id.createId("er"), "Emergency Room"));
        }
        
        @Test
        void testSaveWhenNoDepartmentWithSameIdIsPresentAlreadyShouldAdd() {
            Department toBeInserted = Department.createDepartment(Id.createId("er"), "Emergency Room");
            
            entityManager.getTransaction().begin();
            repository.save(toBeInserted);
            entityManager.getTransaction().commit();
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(toBeInserted);
        }
        
        @Test
        void testDeleteWhenDepartmentIsPresentInDatabaseShouldRemove() {
            addTestDepartmentToDB("sr", "Surgery Room");
            addTestDepartmentToDB("er", "Emergency Room");
            
            entityManager.getTransaction().begin();
            repository.delete(Id.createId("er"));
            entityManager.getTransaction().commit();
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(Department.createDepartment(Id.createId("sr"), "Surgery Room"));
        }
        
        @Test
        void testUpdateWhenDepartmentIsPresentInDatabaseShouldUpdateItsValuesWithTheNewOnes() {
            addTestDepartmentToDB("sr", "Surgery Room");
            addTestDepartmentToDB("er", "Emergency Room");
            
            entityManager.getTransaction().begin();
            repository
                .update(
                    Id.createId("er"),
                    Department.createDepartment(Id.createId("er"), "NEW Emergency Room"));
            entityManager.getTransaction().commit();
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactlyInAnyOrder(
                    Department.createDepartment(Id.createId("sr"), "Surgery Room"),
                    Department.createDepartment(Id.createId("er"), "NEW Emergency Room"));
        }
    }
    
    @Nested @DisplayName("Exceptional cases")
    class ErrorCases {
        
        @Test
        void testSaveWhenDepartmentWithSameIdIsAlreadyPresentInDatabaseShouldThrow() {
            addTestDepartmentToDB("er", "Emergency Room");
            Department newDepartmentWithSameId = Department.createDepartment(Id.createId("er"), "Another Emergency Room");
            
            entityManager.getTransaction().begin();
            
            assertThatExceptionOfType(DuplicateDepartmentException.class)
                .isThrownBy(() -> repository.save(newDepartmentWithSameId));
            
            entityManager.getTransaction().rollback();
            
            assertThat(readAllDepartmentsFromDB())
                .doesNotContain(newDepartmentWithSameId);
        }
        
        @Test
        void testDeleteWhenDepartmentIsNotPresentInDatabaseShouldThrow() {
            Id nonExistentDepartmentId = Id.createId("er");
            
            entityManager.getTransaction().begin();
            
            assertThatExceptionOfType(DepartmentNotFoundException.class)
                .isThrownBy(() -> repository.delete(nonExistentDepartmentId));
            
            entityManager.getTransaction().rollback();
        }
        
        @Test
        void testUpdateWhenDepartmentIsNotPresentInDatabaseShouldThrow() {
            Id nonExistentDepartmentId = Id.createId("er");
            Department newDepartmentWithNonExistentId = Department.createDepartment(nonExistentDepartmentId, "ER");
            
            entityManager.getTransaction().begin();
            
            assertThatExceptionOfType(DepartmentNotFoundException.class)
                .isThrownBy(() -> repository.update(nonExistentDepartmentId, newDepartmentWithNonExistentId));
            
            entityManager.getTransaction().rollback();
        }
    }
    
    private void addTestDepartmentToDB(String id, String name) {
        entityManager.getTransaction().begin();
        entityManager.persist(new DepartmentEntity(id, name));
        entityManager.getTransaction().commit();
        entityManager.clear();
    }
    
    private List<Department> readAllDepartmentsFromDB() {
        entityManager.clear();
        return entityManager.createQuery("SELECT e FROM DepartmentEntity e", DepartmentEntity.class)
            .getResultStream()
            .map(DepartmentEntity::toDepartment)
            .toList();
    }
}
