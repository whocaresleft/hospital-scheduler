package org.duckdns.whocaresleft.repository.mariadb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.mariadb.entity.DoctorEntity;
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

@Testcontainers @DisplayName("Integration tests for MariaDoctorRepository using Testcontainers")
class MariaDoctorRepositoryTestcontainersIT {

    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    private static EntityManagerFactory emf;
    
    private EntityManager entityManager;
    private MariaDoctorRepository repository;
    
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
        
        repository = new MariaDoctorRepository(entityManager);
        
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
        void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllDoctors() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            addTestDoctorToDB("doctor_2", "dok", "ter");
            
            assertThat(repository.findAll())
                .containsExactlyInAnyOrder(
                    Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"),
                    Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
        }
        
        @Test
        void testFindByIdWhenDoctorIsNotPresentInDatabaseShouldReturnNull() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            
            assertThat(repository.findById(Id.createId("doctor_2")))
                .isNull();
        }
        
        @Test
        void testFindByIdWhenDoctorIsPresentInDatabaseShouldReturnSuchDoctor() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            addTestDoctorToDB("doctor_2", "dok", "ter");
            
            assertThat(repository.findById(Id.createId("doctor_2")))
                .isEqualTo(Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter")); 
        }
        @Test
        void testSaveWhenNoDoctorWithSameIdIsAlreadyInDatabaseShouldAdd() {
            Doctor toBeInserted = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
            
            entityManager.getTransaction().begin();
            repository.save(toBeInserted);
            entityManager.getTransaction().commit();
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(toBeInserted);
        }
        
        @Test
        void testDeleteWhenDoctorIsPresentInDatabaseShouldDeleteExistingDoctor() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            
            entityManager.getTransaction().begin();
            repository.delete(Id.createId("doctor_id"));
            entityManager.getTransaction().commit();
            
            assertThat(readAllDoctorsFromDB())
                .isEmpty();
        }
        
        @Test
        void testDeleteWhenDoctorIsPresentInDatabaseAsWellAsAnotherDoctorsShouldDeleteOnlySpecifiedDoctor() {
            addTestDoctorToDB("doctor_id", "doc", "tor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            
            entityManager.getTransaction().begin();
            repository.delete(Id.createId("doctor_id"));
            entityManager.getTransaction().commit();
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
        }
        
        @Test
        void testUpdateWhenDoctorIsPresentInDatabaseShouldUpdateExistingDoctor() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "rotcod");
            
            entityManager.getTransaction().begin();
            repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
            entityManager.getTransaction().commit();
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(newDoctorWithSameId);
        }
        
        @Test
        void testUpdateWhenDoctorIsPresentInDatabaseAsWellAsAnotherDoctorsShouldUpdateOnlySpecifiedDoctor() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");

            entityManager.getTransaction().begin();
            repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
            entityManager.getTransaction().commit();
            
            assertThat(readAllDoctorsFromDB())
                .containsExactlyInAnyOrder(
                    newDoctorWithSameId,
                    Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test
        void testSaveWhenDoctorWithSameIdIsAlreadyInDatabaseShouldThrowAndNotSave() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_1"), "dok", "thor");
            
            entityManager.getTransaction().begin();
            
            assertThatExceptionOfType(DuplicateDoctorException.class)
                .isThrownBy(() -> repository.save(newDoctorWithSameId));
            entityManager.getTransaction().rollback();
            
            assertThat(readAllDoctorsFromDB())
                .doesNotContain(newDoctorWithSameId);
        }
        
        @Test
        void testDeleteWhenDoctorIsNotPresentInDatabaseShouldThrow() {
            Id nonExistendDoctorId = Id.createId("doctor_id");
            
            entityManager.getTransaction().begin();
            
            assertThatExceptionOfType(DoctorNotFoundException.class)
                .isThrownBy(() -> repository.delete(nonExistendDoctorId));
            
            entityManager.getTransaction().rollback();
        }
        
        @Test
        void testUpdateWhenDoctorIsNotPresentInDatabaseShouldThrow() {
            Id validDoctorId = Id.createId("doctor_id");
            Doctor doctorWithNonExistentId = Doctor.createDoctor(Id.createId("doctor_id"), "a", "doctor");
            
            entityManager.getTransaction().begin();
            
            assertThatExceptionOfType(DoctorNotFoundException.class)
                .isThrownBy(() -> repository.update(validDoctorId, doctorWithNonExistentId));
            
            entityManager.getTransaction().rollback();
        }
    }
    
    private void addTestDoctorToDB(String id, String firstName, String lastName) {
        entityManager.getTransaction().begin();
        entityManager.persist(new DoctorEntity(id, firstName, lastName));
        entityManager.getTransaction().commit();
        entityManager.clear();
    }
    
    private List<Doctor> readAllDoctorsFromDB() {
        entityManager.clear();
        return entityManager.createQuery("SELECT e FROM DoctorEntity e", DoctorEntity.class)
            .getResultStream()
            .map(DoctorEntity::toDoctor)
            .toList();
    }
}