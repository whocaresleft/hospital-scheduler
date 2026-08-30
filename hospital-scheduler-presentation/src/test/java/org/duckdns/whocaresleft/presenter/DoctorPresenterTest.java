package org.duckdns.whocaresleft.presenter;

import static java.util.Arrays.asList;
import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.RepositoryProvider;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.transaction.TransactionCode;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.view.DoctorView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.AdditionalAnswers.answer;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@DisplayName("Unit tests for DoctorPresenter")
class DoctorPresenterTest {
    
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    private static final LocalTime TIME_08_30 = LocalTime.of(8, 30);
    private static final LocalTime TIME_09_30 = LocalTime.of(9, 30);

    @Mock
    private TransactionManager transactionManager;
    
    private RepositoryProvider repositoryProvider;
    private DoctorRepository doctorRepository;
    private ShiftRepository shiftRepository;
    
    @Mock
    private DoctorView doctorView;
    
    @InjectMocks
    private DoctorPresenter doctorPresenter;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        
        repositoryProvider = mock(RepositoryProvider.class);
        doctorRepository = mock(DoctorRepository.class);
        shiftRepository = mock(ShiftRepository.class);
        
        when(transactionManager.doInTransaction(any()))
            .thenAnswer(answer((TransactionCode<?> code) -> code.apply(repositoryProvider)));
        
        when(repositoryProvider.getDoctorRepository())
            .thenReturn(doctorRepository);
        when(repositoryProvider.getShiftRepository())
            .thenReturn(shiftRepository);
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
        Doctor nonExistingDoctor = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        
        doctorPresenter.addDoctor(nonExistingDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).save(nonExistingDoctor);
        inOrder.verify(doctorView).doctorAdded(nonExistingDoctor);
    }
    
    @Test
    void testAddDoctorWhenDoctorAlreadyExists() {
        Doctor existingDoctor = Doctor.createDoctor(Id.createId("doctor_1"), "doc", "tor");
        
        doThrow(new DuplicateDoctorException(existingDoctor))
            .when(doctorRepository)
            .save(existingDoctor);
        
        doctorPresenter.addDoctor(existingDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).save(existingDoctor);
        inOrder.verify(doctorView)
            .showErrorDuplicateDoctor(existingDoctor);
    }
    
    @Test
    void testRemoveDoctorWhenDoctorExists() {
        Id existingDoctorId = Id.createId("doctor_1");
        Doctor existingDoctor = Doctor.createDoctor(existingDoctorId, "doc", "tor");
        
        doctorPresenter.removeDoctor(existingDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).delete(existingDoctorId);
        inOrder.verify(doctorView).doctorRemoved(existingDoctor);
    }
    
    @Test
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
            .showErrorDoctorNotFound(nonExistingDoctor);
    }
    
    @Test
    void testUpdateDoctorWhenDoctorExists() {
        Id existingDoctorId = Id.createId("doctor_1");
        Doctor oldDoctor = Doctor.createDoctor(existingDoctorId, "doc", "tor");
        Doctor newDoctor = Doctor.createDoctor(existingDoctorId, "dok", "thor");
        
        doctorPresenter.updateDoctor(oldDoctor, newDoctor);
        
        InOrder inOrder = inOrder(doctorRepository, doctorView);
        inOrder.verify(doctorRepository).update(existingDoctorId, newDoctor);
        inOrder.verify(doctorView).doctorUpdated(oldDoctor, newDoctor);
    }
    
    @Test
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
            .showErrorDoctorNotFound(nonExistingOldDoctor);
    }
    
    @Test
    void testRemoveDoctorShouldAlsoRemoveItsShifts() {
        Id doctorId = Id.createId("doctor_id");
        Doctor doctor = Doctor.createDoctor(doctorId, "doc", "tor");
        
        Shift shift1 = Shift.createShift(doctorId, Id.createId("e_r"), DATE_24_07_2026, TIME_08_00, TIME_08_30);
        Shift shift2 = Shift.createShift(doctorId, Id.createId("surgery"), DATE_24_07_2026, TIME_09_00, TIME_09_30);
        
        when(shiftRepository.findByDoctorId(doctorId))
            .thenReturn(Arrays.asList(shift1, shift2));
        
        doctorPresenter.removeDoctor(doctor);
        
        InOrder inOrder = inOrder(shiftRepository, doctorRepository, shiftRepository, doctorView);
        inOrder.verify(shiftRepository).findByDoctorId(doctorId);
        inOrder.verify(shiftRepository).delete(shift1);
        inOrder.verify(shiftRepository).delete(shift2);
        inOrder.verify(doctorRepository).delete(doctorId);
        inOrder.verify(doctorView).doctorRemoved(doctor);
    }
}
