package org.duckdns.whocaresleft.presenter.mongodb;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.presenter.DepartmentPresenter;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mongodb.MongoTransactionManager;
import org.duckdns.whocaresleft.view.DepartmentView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Testcontainers @DisplayName("Integration tests for DepartmentPresenter with MongoTransactionManager")
class DepartmentPresenterMongoIT {

    private static final String DOCTOR_COLLECTION = "doctor";
    private static final String DEPARTMENT_COLLECTION = "department";
    private static final String SHIFT_COLLECTION = "shift";
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    @Mock
    private DepartmentView view;
    
    private TransactionManager transactionManager;
    private DepartmentPresenter presenter;
    
    private MongoClient client;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        client = MongoClients.create(mongo.getReplicaSetUrl());
        MongoDatabase db = client.getDatabase("hospital");
        
        transactionManager = new MongoTransactionManager(client, db, DOCTOR_COLLECTION, DEPARTMENT_COLLECTION, SHIFT_COLLECTION);
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
        client.close();
        closeable.close();
    }
    
    @Test
    void testAllDepartments() {
        Department d1 = Department.createDepartment(Id.createId("e_r"), "Emergency Room");
        Department d2 = Department.createDepartment(Id.createId("s_r"), "Surgery Room");
        
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
        Department toAdd = Department.createDepartment(Id.createId("er"), "Emergency room");
        
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
