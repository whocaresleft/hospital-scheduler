package org.duckdns.whocaresleft.view;

import java.util.List;

import org.duckdns.whocaresleft.model.Department;

public interface DepartmentView {
    
    public void showAllDepartments(List<Department> departments);
    public void departmentAdded(Department department);
    public void departmentRemoved(Department department);
    public void departmentUpdated(Department oldDepartment, Department newDepartment);
    
    public void showErrorDuplicateDepartment(Department found);
    public void showErrorDepartmentNotFound(Department notFound);
}
