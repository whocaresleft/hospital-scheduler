package org.duckdns.whocaresleft.presenter;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.mongodb.MongoDoctorRepository;
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

@Testcontainers @DisplayName("Integration tests for DoctorPresenter with MongoDoctorRepository")
class DoctorPresenterMongoIT {
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    @Mock
    private DoctorView view;
    private DoctorRepository repository;
    private DoctorPresenter presenter;
    
    private MongoClient client;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        client = MongoClients.create(mongo.getConnectionString());
        
        repository = new MongoDoctorRepository(client);
        for (Doctor d : repository.findAll())
            repository.delete(d.getId());
        
        presenter = new DoctorPresenter(repository, view);
    }
    
    @AfterEach
    void teardown() throws Exception {
        client.close();
        closeable.close();
    } 
    
    @Test
    void testAllDoctors() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        repository.save(doctor);
        
        presenter.allDoctors();
        
        verify(view)
            .showAllDoctors(Arrays.asList(doctor));
    }
    
    @Test
    void testAddDoctor() {
        Doctor doctorToAdd = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        
        presenter.addDoctor(doctorToAdd);
        
        verify(view)
            .doctorAdded(doctorToAdd);
    }
    
    @Test
    void testRemoveDoctor() {
        Doctor doctorToRemove = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        repository.save(doctorToRemove);
        
        presenter.removeDoctor(doctorToRemove);
        
        verify(view)
            .doctorRemoved(doctorToRemove);
    }
    
    @Test
    void testUpdateDoctor() {
        Doctor doctorToUpdate = Doctor.createDoctor(Id.createId("doctor_id"), "doc", "tor");
        repository.save(doctorToUpdate);
        
        Doctor updatedDoctor = Doctor.createDoctor(Id.createId("doctor_id"), "dock", "thor");
        
        presenter.updateDoctor(doctorToUpdate, updatedDoctor);
        
        verify(view)
            .doctorUpdated(doctorToUpdate, updatedDoctor);
    }
}