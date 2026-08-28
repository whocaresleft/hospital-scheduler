package org.duckdns.whocaresleft.presenter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import static java.util.Arrays.asList;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.RepositoryProvider;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.transactions.TransactionCode;
import org.duckdns.whocaresleft.transactions.TransactionManager;
import org.duckdns.whocaresleft.view.DepartmentView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("Unit tests for DepartmentPresenter")
class DepartmentPresenterTest {
    
    private static final LocalDate DATE_24_07_2026 = LocalDate.of(2026, 7, 24);
    private static final LocalTime TIME_08_00 = LocalTime.of(8, 0);
    private static final LocalTime TIME_08_30 = LocalTime.of(8, 30);
    private static final LocalTime TIME_09_30 = LocalTime.of(9, 30);
    
    @Mock
    private TransactionManager transactionManager;
    
    private RepositoryProvider repositoryProvider;
    private DepartmentRepository departmentRepository;
    private ShiftRepository shiftRepository;
    
    @Mock
    private DepartmentView departmentView;
    
    @InjectMocks
    private DepartmentPresenter departmentPresenter;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup () {
        closeable = MockitoAnnotations.openMocks(this);
        
        repositoryProvider = mock(RepositoryProvider.class);
        departmentRepository = mock(DepartmentRepository.class);
        shiftRepository = mock(ShiftRepository.class);
        
        when(transactionManager.doInTransaction(any()))
            .thenAnswer(answer((TransactionCode<?> code) -> code.apply(repositoryProvider)));
    
        when(repositoryProvider.getDepartmentRepository())
            .thenReturn(departmentRepository);
        when(repositoryProvider.getShiftRepository())
        .thenReturn(shiftRepository);
    }
    
    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }
    
    @Test @DisplayName("Method allDepartments()")
    void testAllDepartments() {
        List<Department> departments = asList(Department.createDepartment(Id.createId("er"), "ER"));
        
        when(departmentRepository.findAll())
            .thenReturn(departments);
        
        departmentPresenter.allDepartments();
        
        InOrder inOrder = inOrder(departmentRepository, departmentView);
        inOrder.verify(departmentRepository).findAll();
        inOrder.verify(departmentView).showAllDepartments(departments);
    }
    
    @Test @DisplayName("Method addDepartment(Department) when the department doesn't already exist")
    void testAddDepartmentWhenDepartmentDoesNotAlreadyExist() {
        Department nonExistingDepartment = Department.createDepartment(Id.createId("er"), "ER");
        
        departmentPresenter.addDepartment(nonExistingDepartment);
        
        InOrder inOrder = inOrder(departmentRepository, departmentView);
        inOrder.verify(departmentRepository).save(nonExistingDepartment);
        inOrder.verify(departmentView).departmentAdded(nonExistingDepartment);
    }
    
    @Test @DisplayName("Method addDepartment(Department) when department already exists")
    void testAddDepartmentWhenDepartmentAlreadyExists() {
        Department alreadyExisting = Department.createDepartment(Id.createId("er"), "ER");
        
        doThrow(new DuplicateDepartmentException(alreadyExisting))
            .when(departmentRepository)
            .save(alreadyExisting);
        
        departmentPresenter.addDepartment(alreadyExisting);
        
        InOrder inOrder = inOrder(departmentRepository, departmentView);
        inOrder.verify(departmentRepository).save(alreadyExisting);
        inOrder.verify(departmentView).showDuplicateDepartmentError(alreadyExisting.getId());
    }
    
    @Test @DisplayName("Method removeDepartment(Department) when department exists")
    void testRemoveDepartmentWhenDepartmentExists() {
        Id existingDepartmentId = Id.createId("er");
        Department existingDepartment = Department.createDepartment(existingDepartmentId, "ER");
        
        departmentPresenter.removeDepartment(existingDepartment);
        
        InOrder inOrder = inOrder(departmentRepository, departmentView);
        inOrder.verify(departmentRepository).delete(existingDepartmentId);
        inOrder.verify(departmentView).departmentRemoved(existingDepartment);
    }
    
    @Test @DisplayName("Method removeDepartment(Department) when department doesn't exist")
    void testRemoveDepartmentWhenDepartmentDoesNotExist() {
        Id nonExistingDepartmentId = Id.createId("er");
        Department nonExistingDepartment = Department.createDepartment(nonExistingDepartmentId, "ER");
        
        doThrow(new DepartmentNotFoundException(nonExistingDepartmentId))
            .when(departmentRepository)
            .delete(nonExistingDepartmentId);
        
        departmentPresenter.removeDepartment(nonExistingDepartment);
        
        InOrder inOrder = inOrder(departmentRepository, departmentView);
        inOrder.verify(departmentRepository).delete(nonExistingDepartmentId);
        inOrder.verify(departmentView).showDepartmentNotFoundError(nonExistingDepartmentId);
    }
    
    @Test @DisplayName("Method updateDepartment(Department) when department exists")
    void testUpdateDepartmentWhenDepartmentExists() {
        Id existingDeparmentId = Id.createId("er");
        Department oldDepartment = Department.createDepartment(existingDeparmentId, "old-ER");
        Department newDepartment = Department.createDepartment(existingDeparmentId, "new-ER");
        
        departmentPresenter.updateDepartment(oldDepartment, newDepartment);
        
        InOrder inOrder = inOrder(departmentRepository, departmentView);
        inOrder.verify(departmentRepository).update(existingDeparmentId, newDepartment);
        inOrder.verify(departmentView).departmentUpdated(oldDepartment, newDepartment);
    }
    
    @Test @DisplayName("Method updateDepartment(Department) when department doesn't exist")
    void testUpdateDepartmentWhenDepartmentDoesNotExists() {
        Id nonAlreadyExistingDeparmentId = Id.createId("er");
        Department nonExistingOldDepartment = Department.createDepartment(nonAlreadyExistingDeparmentId, "old-ER");
        Department newDepartment = Department.createDepartment(nonAlreadyExistingDeparmentId, "new-ER");
        
        doThrow(new DepartmentNotFoundException(nonAlreadyExistingDeparmentId))
            .when(departmentRepository)
            .update(nonAlreadyExistingDeparmentId, newDepartment);
        
        departmentPresenter.updateDepartment(nonExistingOldDepartment, newDepartment);
        
        InOrder inOrder = inOrder(departmentRepository, departmentView);
        inOrder.verify(departmentRepository).update(nonAlreadyExistingDeparmentId, newDepartment);
        inOrder.verify(departmentView).showDepartmentNotFoundError(nonAlreadyExistingDeparmentId);
    }
    
    @Test @DisplayName("Method removeDepartment(Department) should also remove its shifts if present")
    void testDeleteDepartmentShouldAlsoRemoveItsShifts() {
        Id departmentId = Id.createId("e_r");
        Department department = Department.createDepartment(departmentId, "ER");
        
        Shift shift1 = Shift.createShift(Id.createId("doc1"), departmentId, DATE_24_07_2026, TIME_08_00, TIME_08_30);
        Shift shift2 = Shift.createShift(Id.createId("doc2"), departmentId, DATE_24_07_2026, TIME_08_00, TIME_09_30);
        
        when(shiftRepository.findByDepartmentId(departmentId))
            .thenReturn(Arrays.asList(shift1, shift2));
        
        departmentPresenter.removeDepartment(department);
        
        InOrder inOrder = inOrder(shiftRepository, departmentRepository, shiftRepository, departmentView);
        inOrder.verify(shiftRepository).findByDepartmentId(departmentId);
        inOrder.verify(shiftRepository).delete(shift1);
        inOrder.verify(shiftRepository).delete(shift2);
        inOrder.verify(departmentRepository).delete(departmentId);
        inOrder.verify(departmentView).departmentRemoved(department);
    }
}
