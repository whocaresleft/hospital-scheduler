package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Doctor;

public class DuplicateDoctorException extends RuntimeException {

    public DuplicateDoctorException(Doctor doctor) {
        super("A doctor with id " + doctor.getId() + " already exists");
    }

    private static final long serialVersionUID = 1L;
}
