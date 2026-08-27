package org.duckdns.whocaresleft.presenter;

import java.util.List;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
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
public class DepartmentPresenterTest {
    
    @Mock
    private DepartmentRepository departmentRepository;
    
    @Mock
    private DepartmentView departmentView;
    
    @InjectMocks
    private DepartmentPresenter departmentPresenter;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setup () {
        closeable = MockitoAnnotations.openMocks(this);
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
}
