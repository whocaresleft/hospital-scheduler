package org.duckdns.whocaresleft.transaction.mariadb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.mariadb.entity.DepartmentEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers @DisplayName("Integration tests for MariaTransactionManager using Testcontainers")
class MariaTransactionManagerTestcontainersIT {
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.11");
    private static EntityManagerFactory emf;
    
    private MariaTransactionManager transactionManager;
    
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
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        em.createQuery("DELETE FROM ShiftEntity").executeUpdate();
        em.createQuery("DELETE FROM DoctorEntity").executeUpdate();
        em.createQuery("DELETE FROM DepartmentEntity").executeUpdate();
        em.getTransaction().commit();
        
        transactionManager = new MariaTransactionManager(emf);
    }
    
    @Nested @DisplayName("Happy cases")
    class HappyCases {
        
        @Test
        void testTransactionThatDoesNotProduceErrorsIsCorrectlyRegisteredToDB() {
            Department toAdd = Department.createDepartment(Id.createId("er"), "ER");
            
            transactionManager.doInTransaction(repositoryProvider -> {
                repositoryProvider.getDepartmentRepository().save(toAdd);
                return null;
            });
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(toAdd);
        }
        
        @Test
        void testTransactionWithMultipleOperationsThatSucceeds() {
            Department toAdd = Department.createDepartment(Id.createId("er"), "ER");
            Department anotherToAdd = Department.createDepartment(Id.createId("sr"), "Surgery Room");
            
            transactionManager.doInTransaction(repositoryProvider -> {
                repositoryProvider.getDepartmentRepository().save(toAdd);
                repositoryProvider.getDepartmentRepository().save(anotherToAdd);
                return null;
            });
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactlyInAnyOrder(toAdd, anotherToAdd);
        }
    }
    
    @Nested @DisplayName("Error cases")
    class ExceptionalCases {
        
        @Test
        void testTransactionThatProducesExceptionIsAbortedNotRegisteredToDBAndForwardsTheException() {
            addTestDepartmentToDB("er", "Old Emergency Room");
            Department toAdd = Department.createDepartment(Id.createId("er"), "New Emergency Room");
            
            assertThatExceptionOfType(DuplicateDepartmentException.class)
                .isThrownBy(() -> 
                    transactionManager.doInTransaction(repositoryProvider -> {
                        repositoryProvider.getDepartmentRepository().save(toAdd);
                        return null;
                }));
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(Department.createDepartment(Id.createId("er"), "Old Emergency Room"));
        }
        
        @Test
        void testTransactionWithMultipleOperationsThatProducesExceptionIsAbortedNotRegisteredToDBAndForwardsTheException() {
            addTestDepartmentToDB("er", "Old Emergency Room");
            Department toAddCorrect = Department.createDepartment(Id.createId("sr"), "Surgery Room");
            Department toAddConflicting = Department.createDepartment(Id.createId("er"), "New Emergency Room");
            
            assertThatExceptionOfType(DuplicateDepartmentException.class)
                .isThrownBy(() -> 
                    transactionManager.doInTransaction(repositoryProvider -> {
                        DepartmentRepository dr = repositoryProvider.getDepartmentRepository();
                        dr.save(toAddCorrect);
                        dr.save(toAddConflicting);
                        return null;
                }));
            
            assertThat(readAllDepartmentsFromDB())
                .containsExactly(Department.createDepartment(Id.createId("er"), "Old Emergency Room"));
        }
    }
    
    private void addTestDepartmentToDB(String id, String name) {
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        em.persist(new DepartmentEntity(id, name));
        em.getTransaction().commit();
        em.close();
    }
    
    private List<Department> readAllDepartmentsFromDB() {
        EntityManager em = emf.createEntityManager();
        List<Department> departments =
            em.createQuery("SELECT e FROM DepartmentEntity e", DepartmentEntity.class)
                .getResultStream()
                .map(DepartmentEntity::toDepartment)
                .toList();
        em.close();
        return departments;
    }
}
