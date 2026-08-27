package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.core.Id;

public class DepartmentNotFoundException extends RuntimeException {
    
    private Id departmentId;
    
    public DepartmentNotFoundException(Id departmentId) {
        super("No department with id " + departmentId + " was found");
        this.departmentId = departmentId;
    }
    
    private static final long serialVersionUID = 1L;

    public Id getDepartmentId() { return departmentId; }
}
