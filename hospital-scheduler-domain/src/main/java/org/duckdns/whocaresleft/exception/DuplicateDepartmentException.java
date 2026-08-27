package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Department;

public class DuplicateDepartmentException extends RuntimeException {
    
    public DuplicateDepartmentException(Department department) {
        super("A department with id " + department.getId() + " already exists");
    }
    
    private static final long serialVersionUID = 1L;
}
