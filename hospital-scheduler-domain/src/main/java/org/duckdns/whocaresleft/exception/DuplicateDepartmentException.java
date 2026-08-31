package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Department;

public class DuplicateDepartmentException extends RuntimeException {
    
    private final transient Department found;
    
    public DuplicateDepartmentException(Department found) {
        super("A department with id " + found.getId() + " already exists");
        this.found = found;
    }
    
    private static final long serialVersionUID = 1L;
    
    public Department getFoundDepartment() { return found; }
}
