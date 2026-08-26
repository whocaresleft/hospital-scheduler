package org.duckdns.whocaresleft.repository.mariadb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
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

@Testcontainers @DisplayName("Integration tests for MariaDoctorRepository using testcontainers")
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
        emf = Persistence.createEntityManagerFactory("maria_doctor_repository_it", properties);
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
        entityManager.createQuery("DELETE FROM DoctorEntity").executeUpdate();
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
        
        @Test @DisplayName("FindAll when database is empty should return empty list")
        void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
            assertThat(repository.findAll())
                .isEmpty();
        }
        
        @Test @DisplayName("FindAll when database is not empty should return all the doctors")
        void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllDoctors() {
            entityManager.getTransaction().begin();
            entityManager.persist(new DoctorEntity("doctor_1", "doc", "tor"));
            entityManager.persist(new DoctorEntity("doctor_2", "dok", "ter"));
            entityManager.getTransaction().commit();
            entityManager.clear();
            
            assertThat(repository.findAll())
                .containsExactly(
                    Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"),
                    Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
        }
        
        @Test @DisplayName("FindById when database doctor is not present should return null")
        void testFindByIdWhenDoctorIsNotPresentInDatabaseShouldReturnNull() {
            entityManager.getTransaction().begin();
            entityManager.persist(new DoctorEntity("doctor_1", "doc", "tor"));
            entityManager.getTransaction().commit();
            entityManager.clear();
            
            assertThat(repository.findById(Id.createId("doctor_2")))
                .isNull();
        }
        
        @Test @DisplayName("FindById when the doctor is present in the database should return the doctor with that id")
        void testFindByIdWhenDoctorIsPresentInDatabaseShouldReturnSuchDoctor() {
            entityManager.getTransaction().begin();
            entityManager.persist(new DoctorEntity("doctor_1", "doc", "tor"));
            entityManager.persist(new DoctorEntity("doctor_2", "dok", "ter"));
            entityManager.getTransaction().commit();
            entityManager.clear();
            
            assertThat(repository.findById(Id.createId("doctor_2")))
                .isEqualTo(Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter")); 
        }
        @Test @DisplayName("Save when the no doctor with the same is already in the database should add")
        void testSaveWhenNoDoctorWithSameIdIsAlreadyInDatabaseShouldAdd() {
            Doctor toBeInserted = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
            
            repository.save(toBeInserted);
            
            List<Doctor> doctors = entityManager.createQuery("SELECT e FROM DoctorEntity e", DoctorEntity.class)
                    .getResultStream()
                    .map(DoctorEntity::toDoctor)
                    .toList();
            assertThat(doctors)
                .containsExactly(toBeInserted);
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test @DisplayName("Save when a doctor with the sane id is already present in the database should throw and not add")
        void testSaveWhenDoctorWithSameIdIsAlreadyInDatabaseShouldThrowAndNotSave() {
            entityManager.getTransaction().begin();
            entityManager.persist(new DoctorEntity("doctor_1", "doc", "tor"));
            entityManager.getTransaction().commit();
            entityManager.clear();
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_1"), "dok", "thor");
            
            assertThatExceptionOfType(DuplicateDoctorException.class)
                .isThrownBy(() -> repository.save(newDoctorWithSameId));
            List<Doctor> doctors = entityManager.createQuery("SELECT e FROM DoctorEntity e", DoctorEntity.class)
                    .getResultStream()
                    .map(DoctorEntity::toDoctor)
                    .toList();
            assertThat(doctors)
                .doesNotContain(newDoctorWithSameId);
        }
    }
}