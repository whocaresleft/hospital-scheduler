package org.duckdns.whocaresleft.presenter;

import static java.util.Arrays.asList;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.view.DoctorView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

@DisplayName("Unit tests for DoctorPresenter")
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
    
    @Test @DisplayName("Method allDoctors()")
    void testAllDoctors() {
        List<Doctor> doctors = asList(Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor"));
        
        when(doctorRepository.findAll())
            .thenReturn(doctors);
        
        doctorPresenter.allDoctors();
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).findAll();
        inOrder.verify(doctorView).showAllDoctors(doctors);
    }
    
    @Test @DisplayName("Method addDoctor(doctor) when doctor doesn't already exist")
    void testAddDoctorWhenDoctorDoesNotAlreadyExist() {
        Doctor nonExistingDoctor = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        
        doctorPresenter.addDoctor(nonExistingDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).save(nonExistingDoctor);
        inOrder.verify(doctorView).doctorAdded(nonExistingDoctor);
    }
    
    @Test @DisplayName("Method addDoctor(doctor) when doctor already exists")
    void testAddDoctorWhenDoctorAlreadyExists() {
        Doctor existingDoctor = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        
        doThrow(new DuplicateDoctorException(existingDoctor))
            .when(doctorRepository)
            .save(existingDoctor);
        
        doctorPresenter.addDoctor(existingDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).save(existingDoctor);
        inOrder.verify(doctorView)
            .showErrorDuplicateDoctor("A doctor with id doctor_1 already exists", existingDoctor);
    }
    
    @Test @DisplayName("Method removeDoctor(doctor) when doctor exists")
    void testRemoveDoctorWhenDoctorExists() {
        Id existingDoctorId = Id.createId("doctor_1");
        Doctor existingDoctor = Doctor.createDoctor(existingDoctorId, "doc", "tor");
        
        doctorPresenter.removeDoctor(existingDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).delete(existingDoctorId);
        inOrder.verify(doctorView).doctorRemoved(existingDoctor);
    }
    
    @Test @DisplayName("Method removeDoctor(doctor) when doctor doesn't exist")
    void testRemoveDoctorWhenDoctorDoesNotExist() {
        Id nonExistingDoctorId = Id.createId("doctor_1");
        Doctor nonExistingDoctor = Doctor.createDoctor(nonExistingDoctorId, "doc", "tor");
        
        doThrow(new DoctorNotFoundException(nonExistingDoctorId))
            .when(doctorRepository)
            .delete(nonExistingDoctorId);
        
        doctorPresenter.removeDoctor(nonExistingDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).delete(nonExistingDoctorId);
        inOrder.verify(doctorView)
            .showErrorDoctorNotFound("No doctor with id doctor_1 was found", nonExistingDoctor);
    }
    
    @Test @DisplayName("Method updateDoctor(oldDoctor, newDoctor) when oldDoctor exists")
    void testUpdateDoctorWhenDoctorExists() {
        Id existingDoctorId = Id.createId("doctor_1");
        Doctor oldDoctor = Doctor.createDoctor(existingDoctorId, "doc", "tor");
        Doctor newDoctor = Doctor.createDoctor(existingDoctorId, "dok", "thor");
        
        doctorPresenter.updateDoctor(oldDoctor, newDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).update(existingDoctorId, newDoctor);
        inOrder.verify(doctorView).doctorUpdated(oldDoctor, newDoctor);
    }
    
    @Test @DisplayName("Method updateDoctor(oldDoctor, newDoctor) when oldDoctor doesn't exist")
    void testUpdateDoctorWhenDoctorDoesNotExist() {
        Id nonExistingDoctorId = Id.createId("doctor_1");
        Doctor nonExistingOldDoctor = Doctor.createDoctor(nonExistingDoctorId, "doc", "tor");
        Doctor newDoctor = Doctor.createDoctor(nonExistingDoctorId, "dok", "thor");
        
        doThrow(new DoctorNotFoundException(nonExistingDoctorId))
            .when(doctorRepository)
            .update(nonExistingDoctorId, newDoctor);
        
        doctorPresenter.updateDoctor(nonExistingOldDoctor, newDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).update(nonExistingDoctorId, newDoctor);
        inOrder.verify(doctorView)
            .showErrorDoctorNotFound("No doctor with id doctor_1 was found", nonExistingOldDoctor);
    }
}
