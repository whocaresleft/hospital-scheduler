package org.duckdns.whocaresleft.repository;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;

public interface DepartmentRepository {
    
    public List<Department> findAll();
    public void save(Department department) throws DuplicateDepartmentException;
    public void delete(Id departmentId) throws DepartmentNotFoundException;
    public void update(Id deparmentId, Department department) throws DepartmentNotFoundException;
}
