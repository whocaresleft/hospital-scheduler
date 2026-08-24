package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.model.Doctor;

public class DoctorNotFoundException extends RuntimeException {

    public DoctorNotFoundException(Doctor doctor) {
        super("No doctor with id " + doctor.getId() + " was found");
    }

    private static final long serialVersionUID = 1L;
}
