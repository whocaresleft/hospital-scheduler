package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Department;

public class DuplicateDepartmentException extends RuntimeException {
    
    private final transient Department department;
    
    public DuplicateDepartmentException(Department department) {
        super("A department with id " + department.getId() + " already exists");
        this.department = department;
    }
    
    private static final long serialVersionUID = 1L;
    
    public Department getDepartment() { return department; }
}
