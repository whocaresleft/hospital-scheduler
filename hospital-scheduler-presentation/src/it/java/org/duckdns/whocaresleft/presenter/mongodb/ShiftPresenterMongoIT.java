package org.duckdns.whocaresleft.presenter.mongodb;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.presenter.ShiftPresenter;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mongodb.MongoTransactionManager;
import org.duckdns.whocaresleft.view.ShiftView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Testcontainers @DisplayName("Integration tests for ShiftPresenter with MongoTransactionManager")
class ShiftPresenterMongoIT {
    
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_08_30 = LocalTime.of(8, 30);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    
    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:5");
    
    @Mock
    private ShiftView view;
    
    private TransactionManager transactionManager;
    private ShiftPresenter presenter;
    
    private MongoClient client;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        client = MongoClients.create(mongo.getReplicaSetUrl());
        MongoDatabase db = client.getDatabase("hospital");
        
        transactionManager = new MongoTransactionManager(client, db);
        transactionManager.doInTransaction(provider -> {
            DoctorRepository doctorRepo = provider.getDoctorRepository();
            DepartmentRepository departmentRepo = provider.getDepartmentRepository();
            ShiftRepository shiftRepo = provider.getShiftRepository();
            
            for (Shift s : shiftRepo.findAll())
                shiftRepo.delete(s);
            
            for (Doctor d : doctorRepo.findAll())
                doctorRepo.delete(d.getId());
            
            for (Department d : departmentRepo.findAll())
                departmentRepo.delete(d.getId());
            
            return null;
        });
        
        presenter = new ShiftPresenter(transactionManager, view);
    }
    
    @AfterEach
    void teardown() throws Exception {
        client.close();
        closeable.close();
    }
    
    @Test
    void testAllShifts() {
        Shift shift1 = Shift.createShift(
            Id.createId("doctor_id"), Id.createId("department_id"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
        
        Shift shift2 = Shift.createShift(
            Id.createId("doctor_id"), Id.createId("department_id"), DATE_24_07_2026, TIME_08_30, TIME_09_00);
        
        transactionManager.doInTransaction(provider -> {
            ShiftRepository repository = provider.getShiftRepository();
            repository.save(shift1);
            repository.save(shift2);
            return null;
        });
        
        presenter.allShifts();
        
        verify(view)
            .showAllShifts(Arrays.asList(shift1, shift2));
    }
    
    @Test
    void testAddShift() {
        Shift toAdd = Shift.createShift(
            Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
        
        transactionManager.doInTransaction(provider -> {
            provider.getDepartmentRepository()
                .save(Department.createDepartment(Id.createId("er"), "Emergency Room"));
            provider.getDoctorRepository()
                .save(Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor"));
            
            return null;
        });
        
        presenter.addShift(toAdd);
        
        verify(view)
            .shiftAdded(toAdd);
    }
    
    @Test
    void testRemoveShift() {
        Shift toRemove = Shift.createShift(
            Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
        transactionManager.doInTransaction(provider -> {
            provider.getShiftRepository().save(toRemove);
            return null;
        });
        
        presenter.removeShift(toRemove);
        
        verify(view)
            .shiftRemoved(toRemove);
    }
    
    @Test
    void testUpdateShift() {
        Shift toUpdate = Shift.createShift(
            Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
        transactionManager.doInTransaction(provider -> {
            provider.getDepartmentRepository()
                .save(Department.createDepartment(Id.createId("er"), "Emergency Room"));
            
            provider.getDoctorRepository()
                .save(Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor"));
            
            provider.getShiftRepository().save(toUpdate);
            return null;
        });
        
        Shift updated = Shift.createShift(
            Id.createId("doctor_id"), Id.createId("er"), DATE_24_07_2026, TIME_08_30, TIME_09_00);
        
        presenter.updateShift(toUpdate, updated);
        
        verify(view)
            .shiftUpdated(toUpdate, updated);
    }
}
