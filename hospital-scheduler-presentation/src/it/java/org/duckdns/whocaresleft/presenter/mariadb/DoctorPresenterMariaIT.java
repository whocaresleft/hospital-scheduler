package org.duckdns.whocaresleft.presenter.mariadb;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.transaction.mariadb.MariaTransactionManager;
import org.duckdns.whocaresleft.view.DoctorView;
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

@Testcontainers @DisplayName("Integration tests for DoctorPresenter with MariaTransactionManager")
class DoctorPresenterMariaIT {
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.11");
    private static EntityManagerFactory emf;
    
    @Mock
    private DoctorView view;
    
    private DoctorPresenter presenter;
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
            DoctorRepository repository = provider.getDoctorRepository();
            
            for (Doctor d : repository.findAll())
                repository.delete(d.getId());
            
            return null;
        });
        
        presenter = new DoctorPresenter(transactionManager, view);
    }
    
    @AfterEach
    void teardown() throws Exception {
        closeable.close();
    }
    
    @Test
    void testAllDoctors() {
        Doctor d1 = Doctor.createDoctor(Id.createId("doctor_1"), "Doc", "Tor");
        Doctor d2 = Doctor.createDoctor(Id.createId("doctor_2"), "Dok", "Thor");
        
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
        Doctor toAdd = Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor");
        
        presenter.addDoctor(toAdd);
        
        verify(view)
            .doctorAdded(toAdd);
    }
    
    @Test
    void testRemoveDoctor() {
        Doctor toRemove = Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor");
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
        Doctor toUpdate = Doctor.createDoctor(Id.createId("doctor_id"), "Doc", "Tor");
        transactionManager.doInTransaction(provider -> {
            provider.getDoctorRepository().save(toUpdate);
            return null;
        });
        
        Doctor updated = Doctor.createDoctor(Id.createId("doctor_id"), "Dok", "Ter");
        
        presenter.updateDoctor(toUpdate, updated);
        
        verify(view)
            .doctorUpdated(toUpdate, updated);
    }
}
