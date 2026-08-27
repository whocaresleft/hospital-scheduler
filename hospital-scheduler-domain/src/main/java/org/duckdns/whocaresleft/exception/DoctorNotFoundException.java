package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.core.Id;

public class DoctorNotFoundException extends RuntimeException {
    
    public DoctorNotFoundException(Id doctorId) {
        super("No doctor with id " + doctorId + " was found");
    }
    
    private static final long serialVersionUID = 1L;
}
