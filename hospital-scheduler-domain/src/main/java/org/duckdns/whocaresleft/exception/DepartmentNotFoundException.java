package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.core.Id;

public class DepartmentNotFoundException extends RuntimeException {
    
    public DepartmentNotFoundException(Id departmentId) {
        super("No department with id " + departmentId + " was found");
    }
    
    private static final long serialVersionUID = 1L;
    
}
