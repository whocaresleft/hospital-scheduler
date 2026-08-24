package org.duckdns.whocaresleft.presenter;

import static java.util.Arrays.asList;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.view.DoctorView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

class DoctorPresenterTest {

    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private DoctorView doctorView;
    
    @InjectMocks
    private DoctorPresenter doctorPresenter;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }
    
    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }
    
    @Test
    void testAllDoctors() {
        List<Doctor> doctors = asList(Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"));
        
        when(doctorRepository.findAll())
            .thenReturn(doctors);
        
        doctorPresenter.allDoctors();
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).findAll();
        inOrder.verify(doctorView).showAllDoctors(doctors);
    }
    
    @Test
    void testAddDoctorWhenDoctorDoesNotAlreadyExist() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        
        doctorPresenter.addDoctor(doctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).save(doctor);
        inOrder.verify(doctorView).doctorAdded(doctor);
    }
    
    @Test
    void testAddDoctorWhenDoctorAlreadyExisst() {
        Doctor doctor = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        
        doThrow(new DuplicateDoctorException(doctor))
            .when(doctorRepository)
            .save(doctor);
        
        doctorPresenter.addDoctor(doctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).save(doctor);
        inOrder.verify(doctorView).showError(argThat(s -> s.contains("doctor_1")));
    }
}
