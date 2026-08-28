package org.duckdns.whocaresleft.repository;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;

public interface DepartmentRepository {
    
    List<Department> findAll();
    Department findById(Id id);
    void save(Department department) throws DuplicateDepartmentException;
    void delete(Id departmentId) throws DepartmentNotFoundException;
    void update(Id departmentId, Department newDepartment) throws DepartmentNotFoundException;
}
