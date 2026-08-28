package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Doctor;

public class DuplicateDoctorException extends RuntimeException {
    
    private final transient Doctor doctor;
    
    public DuplicateDoctorException(Doctor doctor) {
        super("A doctor with id " + doctor.getId() + " already exists");
        this.doctor = doctor;
    }
    
    private static final long serialVersionUID = 1L;
    
    public Doctor getDoctor() { return doctor; }
}
