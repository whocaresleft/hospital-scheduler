package org.duckdns.whocaresleft.presenter.mongodb;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mongodb.MongoTransactionManager;
import org.duckdns.whocaresleft.view.DoctorView;
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

@Testcontainers @DisplayName("Integration tests for DoctorPresenter with MongoTransactionManager")
class DoctorPresenterMongoIT {
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    @Mock
    private DoctorView view;
    
    private TransactionManager transactionManager;
    private DoctorPresenter presenter;
    
    private MongoClient client;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        client = MongoClients.create(mongo.getReplicaSetUrl());
        MongoDatabase db = client.getDatabase("hospital");
        
        transactionManager = new MongoTransactionManager(client, db);
        transactionManager.doInTransaction(provider -> {
            DoctorRepository docRepo = provider.getDoctorRepository();
            ShiftRepository shRepo = provider.getShiftRepository();
            
            for (Shift s : shRepo.findAll())
                shRepo.delete(s);
            
            for (Doctor d : docRepo.findAll())
                docRepo.delete(d.getId());
            
            return null;
        });
        
        presenter = new DoctorPresenter(transactionManager, view);
    }
    
    @AfterEach
    void teardown() throws Exception {
        client.close();
        closeable.close();
    }
    
    @Test
    void testAllDoctors() {
        Doctor d1 = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        Doctor d2 = Doctor.createDoctor(Id.createId("doctor_2"), "dok", "ter");
        
        transactionManager.doInTransaction(provider -> {
            DoctorRepository repository = provider.getDoctorRepository();
            repository.save(d1);
            repository.save(d2);
            return null;
        });
        
        presenter.allDoctors();
        
        verify(view)
            .showAllDoctors(Arrays.asList(d1, d2));
    }
    
    @Test
    void testAddDoctor() {
        Doctor toAdd = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        
        presenter.addDoctor(toAdd);
        
        verify(view)
            .doctorAdded(toAdd);
    }
    
    @Test
    void testRemoveDoctor() {
        Doctor toRemove = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(toRemove);
            return null;
        });
        
        presenter.removeDoctor(toRemove);
        
        verify(view)
            .doctorRemoved(toRemove);
    }
    
    @Test
    void testUpdateDoctor() {
        Doctor toUpdate = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(toUpdate);
            return null;
        });
        
        Doctor updated = Doctor.createDoctor(Id.createId("doctor_id"), "dock", "thor");
        
        presenter.updateDoctor(toUpdate, updated);
        
        verify(view)
            .doctorUpdated(toUpdate, updated);
    }
}
