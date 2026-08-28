package org.duckdns.whocaresleft.presenter;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.ShiftNotFoundException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.transaction.TransactionCode;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.RepositoryProvider;
import org.duckdns.whocaresleft.view.ShiftView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.AdditionalAnswers.answer;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@DisplayName("Unit tests for ShiftPresenter")
class ShiftPresenterTest {
    
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_09_00 = LocalTime.of(9, 0);
    private static final LocalTime TIME_08_30 = LocalTime.of(8, 30);
    private static final LocalTime TIME_09_30 = LocalTime.of(9, 30);
    
    @Mock
    private TransactionManager transactionManager;
    
    private RepositoryProvider repositoryProvider;
    private ShiftRepository shiftRepository;
    private DepartmentRepository departmentRepository;
    private DoctorRepository doctorRepository;
    
    @Mock
    private ShiftView shiftView;
    
    @InjectMocks
    private ShiftPresenter shiftPresenter;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        
        repositoryProvider = mock(RepositoryProvider.class);
        shiftRepository = mock(ShiftRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        
        when(transactionManager.doInTransaction(any()))
            .thenAnswer(answer((TransactionCode<?> code) -> code.apply(repositoryProvider)));
        
        when(repositoryProvider.getShiftRepository())
            .thenReturn(shiftRepository);
        when(repositoryProvider.getDepartmentRepository())
            .thenReturn(departmentRepository);
        when(repositoryProvider.getDoctorRepository())
            .thenReturn(doctorRepository);
    }
    
    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }
    
    @Test
    void testAllShifts() {
        Id doctorId = Id.createId("doctor_id");
        Id departmentId = Id.createId("department_id");
        
        List<Shift> shifts = asList(
            Shift.createShift(doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00));
        
        when(shiftRepository.findAll())
            .thenReturn(shifts);
        
        shiftPresenter.allShifts();
        
        InOrder inOrder = inOrder(shiftRepository, shiftView);
        inOrder.verify(shiftRepository).findAll();
        inOrder.verify(shiftView).showAllShifts(shifts);
    }
    
    @Test
    void testAddShiftWhenDoctorDoesNotExist() {
        Id doctorId = Id.createId("doctor");
        Id departmentId = Id.createId("sur_1");
        Shift shift = Shift.createShift(doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(null);
        
        shiftPresenter.addShift(shift);
        
        verify(shiftView).showErrorDoctorNotFound(doctorId);
        verifyNoMoreInteractions(ignoreStubs(shiftRepository));
        verifyNoMoreInteractions(ignoreStubs(doctorRepository));
        verifyNoMoreInteractions(ignoreStubs(departmentRepository));
    }
    
    @Test
    void testAddShiftWhenDepartmentDoesNotExist() {
        Id doctorId = Id.createId("doctor");
        Doctor doctor = Doctor.createDoctor(doctorId, "doc", "tor");
        Id departmentId = Id.createId("sur_1");
        Shift shift = Shift.createShift(doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(doctor);
        when(departmentRepository.findById(departmentId))
            .thenReturn(null);
        
        shiftPresenter.addShift(shift);
        
        verify(shiftView).showErrorDepartmentNotFound(departmentId);
        verifyNoMoreInteractions(ignoreStubs(shiftRepository));
        verifyNoMoreInteractions(ignoreStubs(doctorRepository));
        verifyNoMoreInteractions(ignoreStubs(departmentRepository));
    }
    
    @Test
    void testAddShiftWhenDoctorAndDepartmentExistAndShiftDoesNotOverlap() {
        Id doctorId = Id.createId("doctor");
        Doctor doctor = Doctor.createDoctor(doctorId, "doc", "tor");
        Id departmentId = Id.createId("sur_1");
        Department department = Department.createDepartment(departmentId, "Surgery room");
        Shift shift = Shift.createShift(doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(doctor);
        when(departmentRepository.findById(departmentId))
            .thenReturn(department);
        when(shiftRepository.findByDoctorId(doctorId))
            .thenReturn(Collections.emptyList());
        
        shiftPresenter.addShift(shift);
        
        InOrder inOrder = inOrder(shiftRepository, shiftView);
        inOrder.verify(shiftRepository).save(shift);
        inOrder.verify(shiftView).shiftAdded(shift);
    }
    
    @Test
    void testAddShiftForDoctorWhenAnotherExistingShiftForSameDoctorExistsInSameDepartment() {
        Id doctorId = Id.createId("doctor");
        Doctor doctor = Doctor.createDoctor(doctorId, "doc", "tor");
        Id departmentId = Id.createId("sur_1");
        Department department = Department.createDepartment(departmentId, "Surgery room");
        
        Shift originalConflictingShift = Shift.createShift(doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        Shift nonConflictingShift = Shift.createShift(doctorId, departmentId, DATE_24_07_2026, TIME_09_00, TIME_09_30);
        Shift overlappingShift = Shift.createShift(doctorId, departmentId, DATE_24_07_2026, TIME_08_30, TIME_09_00);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(doctor);
        when(departmentRepository.findById(departmentId))
            .thenReturn(department);
        when(shiftRepository.findByDoctorId(doctorId))
            .thenReturn(Arrays.asList(nonConflictingShift, originalConflictingShift));
        
        shiftPresenter.addShift(overlappingShift);

        verify(shiftView).showErrorOverlappedShift(originalConflictingShift, overlappingShift);
        verifyNoMoreInteractions(ignoreStubs(shiftRepository));
    }
    
    @Test
    void testAddShiftWhenShiftOverlapsWithAnExistingShiftInAnyDepartment() {
        Id doctorId = Id.createId("doctor");
        Doctor doctor = Doctor.createDoctor(doctorId, "doc", "tor");
        Id originalDepartmentId = Id.createId("sur_1");
        
        Id overlappedDepartmentId = Id.createId("ER");
        Department overlappedDepartment = Department.createDepartment(overlappedDepartmentId, "er");
        
        Shift conflictingShift = Shift.createShift(doctorId, originalDepartmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        Shift nonConflictingShift = Shift.createShift(doctorId, originalDepartmentId, DATE_24_07_2026, TIME_09_00, TIME_09_30);
        Shift overlappingShift = Shift.createShift(doctorId, overlappedDepartmentId, DATE_24_07_2026, TIME_08_30, TIME_09_00);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(doctor);
        when(departmentRepository.findById(overlappedDepartmentId))
            .thenReturn(overlappedDepartment);
        when(shiftRepository.findByDoctorId(doctorId))
            .thenReturn(Arrays.asList(conflictingShift, nonConflictingShift));
        
        shiftPresenter.addShift(overlappingShift);

        verify(shiftView).showErrorOverlappedShift(conflictingShift, overlappingShift);
        verifyNoMoreInteractions(ignoreStubs(shiftRepository));
    }
    
    @Test
    void testRemoveShiftWhenShiftExists() {
        Id doctorId = Id.createId("doctor_id");
        Id departmentId = Id.createId("department_id");
        Shift existingShift = Shift.createShift(
                doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        
        shiftPresenter.removeShift(existingShift);
        
        InOrder inOrder = inOrder(shiftRepository, shiftView);
        inOrder.verify(shiftRepository).delete(existingShift);
        inOrder.verify(shiftView).shiftRemoved(existingShift);
    }
    
    @Test
    void testRemoveShiftWhenShiftDoesNotExist() {
        Id doctorId = Id.createId("doctor_id");
        Id departmentId = Id.createId("department_id");
        Shift nonExistingShift = Shift.createShift(
                doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        
        doThrow(new ShiftNotFoundException(nonExistingShift))
            .when(shiftRepository)
            .delete(nonExistingShift);
        
        shiftPresenter.removeShift(nonExistingShift);
        
        InOrder inOrder = inOrder(shiftRepository, shiftView);
        inOrder.verify(shiftRepository).delete(nonExistingShift);
        inOrder.verify(shiftView)
            .showErrorShiftNotFound(nonExistingShift);
    }
    
    @Test
    void testUpdateShiftWhenNewDoctorDoesNotExist() {
        Id existentDoctorId = Id.createId("doctor_id_exist");
        Id nonExistentDoctorId = Id.createId("doctor_id_non_exist");
        
        Id departmentId = Id.createId("department_id");
        
        Shift oldShift = Shift.createShift(
                existentDoctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        Shift newShift = Shift.createShift(
                nonExistentDoctorId, departmentId, DATE_24_07_2026, TIME_08_30, TIME_09_30);
        
        when(doctorRepository.findById(nonExistentDoctorId))
            .thenReturn(null);
        
        shiftPresenter.updateShift(oldShift, newShift);
        
        InOrder inOrder = inOrder(doctorRepository, shiftView);
        inOrder.verify(doctorRepository).findById(nonExistentDoctorId);
        inOrder.verify(shiftView).showErrorDoctorNotFound(nonExistentDoctorId);
        verifyNoMoreInteractions(ignoreStubs(shiftRepository));
        verifyNoMoreInteractions(ignoreStubs(doctorRepository));
        verifyNoMoreInteractions(ignoreStubs(departmentRepository));
    }
    
    @Test
    void testUpdateShiftWhenNewDepartmentDoesNotExist() {
        Id doctorId = Id.createId("doctor_id");
        Id existendDepartmentId = Id.createId("er");
        Id nonExistentDepartmentId = Id.createId("non_existing_room");
        
        Shift oldShift = Shift.createShift(
                doctorId, existendDepartmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        Shift newShift = Shift.createShift(
                doctorId, nonExistentDepartmentId, DATE_24_07_2026, TIME_08_30, TIME_09_30);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(Doctor.createDoctor(doctorId, "doc", "tor"));
        when(departmentRepository.findById(nonExistentDepartmentId))
            .thenReturn(null);
        
        shiftPresenter.updateShift(oldShift, newShift);
        
        InOrder inOrder = inOrder(departmentRepository, shiftView);
        inOrder.verify(departmentRepository).findById(nonExistentDepartmentId);
        inOrder.verify(shiftView).showErrorDepartmentNotFound(nonExistentDepartmentId);
        verifyNoMoreInteractions(ignoreStubs(shiftRepository));
        verifyNoMoreInteractions(ignoreStubs(doctorRepository));
        verifyNoMoreInteractions(ignoreStubs(departmentRepository));
    }
    
    @Test
    void testUpdateShiftWhenShiftExistsAndDoesNotOverlap() {
        Id doctorId = Id.createId("doctor_id");
        Id departmentId = Id.createId("department_id");
        Shift oldShift = Shift.createShift(
                doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        Shift newShift = Shift.createShift(
                doctorId, departmentId, DATE_24_07_2026, TIME_08_30, TIME_09_30);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(Doctor.createDoctor(doctorId, "doc", "tor"));
        when(departmentRepository.findById(departmentId))
            .thenReturn(Department.createDepartment(departmentId, "er"));
        
        when(shiftRepository.findByDoctorId(doctorId))
            .thenReturn(Collections.emptyList());
        
        shiftPresenter.updateShift(oldShift, newShift);
        
        InOrder inOrder = inOrder(shiftRepository, shiftView);
        inOrder.verify(shiftRepository).update(oldShift, newShift);
        inOrder.verify(shiftView).shiftUpdated(oldShift, newShift);
    }
    
    @Test
    void testUpdateShiftWhenShiftExistsAndOverlaps() {
        Id doctorId = Id.createId("doctor_id");
        Id departmentId1 = Id.createId("department_1");
        Id departmentId2 = Id.createId("department_2");
        
        Shift originalShift = Shift.createShift(
                doctorId, departmentId1, DATE_24_07_2026, TIME_08_00, TIME_08_30);
        Shift conflictingShift = Shift.createShift(
                doctorId, departmentId2, DATE_24_07_2026, TIME_09_00, TIME_09_30);
        
        Shift newShift = Shift.createShift(
                doctorId, departmentId2, DATE_24_07_2026, TIME_08_30, TIME_09_30);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(Doctor.createDoctor(doctorId, "doc", "tor"));
        when(departmentRepository.findById(departmentId2))
            .thenReturn(Department.createDepartment(departmentId2, "er"));
        
        when(shiftRepository.findByDoctorId(doctorId))
            .thenReturn(Arrays.asList(originalShift, conflictingShift));
        
        shiftPresenter.updateShift(originalShift, newShift);
        
        verify(shiftView).showErrorOverlappedShift(conflictingShift, newShift);
        verifyNoMoreInteractions(ignoreStubs(shiftRepository));
    }
    
    @Test
    void testUpdateShiftWhenShiftDoesNotExist() {
        Id doctorId = Id.createId("doctor_id");
        Id departmentId = Id.createId("department_id");
        Shift oldNonExistentShift = Shift.createShift(
                doctorId, departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_00);
        Shift newShift = Shift.createShift(
                doctorId, departmentId, DATE_24_07_2026, TIME_08_30, TIME_09_30);
        
        when(doctorRepository.findById(doctorId))
            .thenReturn(Doctor.createDoctor(doctorId, "doc", "tor"));
        when(departmentRepository.findById(departmentId))
            .thenReturn(Department.createDepartment(departmentId, "er"));
        doThrow(new ShiftNotFoundException(oldNonExistentShift))
            .when(shiftRepository)
            .update(oldNonExistentShift, newShift);
        
        shiftPresenter.updateShift(oldNonExistentShift, newShift);
        
        InOrder inOrder = inOrder(shiftRepository, shiftView);
        inOrder.verify(shiftRepository).update(oldNonExistentShift, newShift);
        inOrder.verify(shiftView)
            .showErrorShiftNotFound(oldNonExistentShift);
    }
}
