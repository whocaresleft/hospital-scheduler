package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Doctor;

public class DuplicateDoctorException extends RuntimeException {
    
    private final transient Doctor found;
    
    public DuplicateDoctorException(Doctor found) {
        super("A doctor with id " + found.getId() + " already exists");
        this.found = found;
    }
    
    private static final long serialVersionUID = 1L;
    
    public Doctor getFoundDoctor() { return found; }
}
