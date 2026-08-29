package org.duckdns.whocaresleft.presenter.mariadb;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.presenter.DepartmentPresenter;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mariadb.MariaTransactionManager;
import org.duckdns.whocaresleft.view.DepartmentView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers @DisplayName("Integration tests for DepartmentPresenter with MariaTransactionManager")
class DepartmentPresenterMariaIT {
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    private static EntityManagerFactory emf;
    
    @Mock
    private DepartmentView view;
    
    private DepartmentPresenter presenter;
    private TransactionManager transactionManager;
    
    private AutoCloseable closeable;
    
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
        closeable = MockitoAnnotations.openMocks(this);
        
        transactionManager = new MariaTransactionManager(emf);
        transactionManager.doInTransaction(provider -> {
            DepartmentRepository repository = provider.getDepartmentRepository();
            
            for (Department d : repository.findAll())
                repository.delete(d.getId());
            
            return null;
        });
        
        presenter = new DepartmentPresenter(transactionManager, view);
    }
    
    @AfterEach
    void teardown() throws Exception {
        closeable.close();
    }
    
    @Test
    void testAllDepartments() {
        Department d1 = Department.createDepartment(Id.createId("er"), "Emergency Room");
        Department d2 = Department.createDepartment(Id.createId("sr"), "Surgery Room");
        
        transactionManager.doInTransaction(provider -> {
            DepartmentRepository repository = provider.getDepartmentRepository();
            repository.save(d1);
            repository.save(d2);
            return null;
        });
        
        presenter.allDepartments();
        
        verify(view)
            .showAllDepartments(Arrays.asList(d1, d2));
    }
    
    @Test
    void testAddDepartment() {
        Department toAdd = Department.createDepartment(Id.createId("er"), "Emergency Room");
        
        presenter.addDepartment(toAdd);
        
        verify(view)
            .departmentAdded(toAdd);
    }
    
    @Test
    void testRemoveDepartment() {
        Department toRemove = Department.createDepartment(Id.createId("er"), "Emergency Room");
        transactionManager.doInTransaction(provider -> {
            provider.getDepartmentRepository().save(toRemove);
            return null;
        });
        
        presenter.removeDepartment(toRemove);
        
        verify(view)
            .departmentRemoved(toRemove);
    }
    
    @Test
    void testUpdateDepartment() {
        Department toUpdate = Department.createDepartment(Id.createId("er"), "Old Emergency Room");
        transactionManager.doInTransaction(provider -> {
            provider.getDepartmentRepository().save(toUpdate);
            return null;
        });
        
        Department updated = Department.createDepartment(Id.createId("er"), "New Emergency Room");
        
        presenter.updateDepartment(toUpdate, updated);
        
        verify(view)
            .departmentUpdated(toUpdate, updated);
    }
}
