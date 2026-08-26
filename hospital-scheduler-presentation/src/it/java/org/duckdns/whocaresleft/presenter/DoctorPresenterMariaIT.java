package org.duckdns.whocaresleft.presenter;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Map;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.mariadb.MariaDoctorRepository;
import org.duckdns.whocaresleft.view.DoctorView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Testcontainers @DisplayName("Integration tests for DoctorPresenter with MariaDoctorRepository")
class DoctorPresenterMariaIT {
    
    @Container
    private static final MariaDBContainer<?> maria = new MariaDBContainer<>("mariadb:10.3.39");
    
    @Mock
    private DoctorView view;
    private DoctorRepository repository;
    private DoctorPresenter presenter;

    private static EntityManagerFactory emf;
    private EntityManager entityManager;
    private AutoCloseable closeable;
    
    @BeforeAll
    static void setupEntityManagerFactory() {
        Map<String, String> properties = Map.of(
            "jakarta.persistence.jdbc.url", maria.getJdbcUrl(),
            "jakarta.persistence.jdbc.user", maria.getUsername(),
            "jakarta.persistence.jdbc.password", maria.getPassword(),
            "jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver",
            "hibernate.hbm2ddl.auto", "create-drop");
        emf = Persistence.createEntityManagerFactory("maria_doctor_presenter_it", properties);
    }
    
    @AfterAll
    static void teardownEntityManagerFactory() {
        if (emf != null)
            emf.close();
    }
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        entityManager = emf.createEntityManager();
        
        repository = new MariaDoctorRepository(entityManager);
        for (Doctor d : repository.findAll())
            repository.delete(d.getId());
        
        presenter = new DoctorPresenter(repository, view);
    }
    
    @AfterEach
    void teardown() throws Exception {
        if (entityManager.isOpen()) {
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            entityManager.close();
        }
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