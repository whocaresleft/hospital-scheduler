package org.duckdns.whocaresleft.repository.mariadb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
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
            addTestDoctorToDB("doctor_1", "doc", "tor");
            addTestDoctorToDB("doctor_2", "dok", "ter");
            
            assertThat(repository.findAll())
                .containsExactly(
                    Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"),
                    Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter"));
        }
        
        @Test @DisplayName("FindById when database doctor is not present should return null")
        void testFindByIdWhenDoctorIsNotPresentInDatabaseShouldReturnNull() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            
            assertThat(repository.findById(Id.createId("doctor_2")))
                .isNull();
        }
        
        @Test @DisplayName("FindById when the doctor is present in the database should return the doctor with that id")
        void testFindByIdWhenDoctorIsPresentInDatabaseShouldReturnSuchDoctor() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            addTestDoctorToDB("doctor_2", "dok", "ter");
            
            assertThat(repository.findById(Id.createId("doctor_2")))
                .isEqualTo(Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter")); 
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
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "rotcod");
            
            repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(newDoctorWithSameId);
        }
        
        @Test @DisplayName("Update when the doctor is present in the database, as well as other doctors, should only update the specified one")
        void testUpdateWhenDoctorIsPresentInDatabaseAsWellAsAnotherDoctorsShouldUpdateOnlySpecifiedDoctor() {
            addTestDoctorToDB("doctor_id", "original", "doctor");
            addTestDoctorToDB("doctor_id2", "dok", "ter");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_id"), "a new", "doctor");
            
            repository.update(Id.createId("doctor_id"), newDoctorWithSameId);
            
            assertThat(readAllDoctorsFromDB())
                .containsExactly(
                    newDoctorWithSameId,
                    Doctor.createDoctor(Id.createId("doctor_id2"), "dok", "ter"));
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test @DisplayName("Save when a doctor with the sane id is already present in the database should throw and not add")
        void testSaveWhenDoctorWithSameIdIsAlreadyInDatabaseShouldThrowAndNotSave() {
            addTestDoctorToDB("doctor_1", "doc", "tor");
            Doctor newDoctorWithSameId = Doctor.createDoctor(Id.createId("doctor_1"), "dok", "thor");
            
            assertThatExceptionOfType(DuplicateDoctorException.class)
                .isThrownBy(() -> repository.save(newDoctorWithSameId));
            assertThat(readAllDoctorsFromDB())
                .doesNotContain(newDoctorWithSameId);
        }
        
        @Test @DisplayName("Delete when no doctor with the specified id is in the database should throw")
        void testDeleteWhenDoctorIsNotPresentInDatabaseShouldThrow() {
            Id nonExistendDoctorId = Id.createId("doctor_id");
            
            assertThatExceptionOfType(DoctorNotFoundException.class)
                .isThrownBy(() -> repository.delete(nonExistendDoctorId));
            assertThat(entityManager.getTransaction().isActive())
                .isFalse();
        }
        
        @Test @DisplayName("Update when no doctor with the specified id is in the database should throw")
        void testUpdateWhenDoctorIsNotPresentInDatabaseShouldThrow() {
            Id validDoctorId = Id.createId("doctor_id");
            Doctor doctorWithNonExistentId = Doctor.createDoctor(Id.createId("doctor_id"), "a", "doctor");
            
            assertThatExceptionOfType(DoctorNotFoundException.class)
                .isThrownBy(() -> repository.update(validDoctorId, doctorWithNonExistentId));
            assertThat(entityManager.getTransaction().isActive())
                .isFalse();
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