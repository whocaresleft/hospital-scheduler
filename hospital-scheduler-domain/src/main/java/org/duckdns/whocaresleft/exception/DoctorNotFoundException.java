package org.duckdns.whocaresleft.exception;

import org.duckdns.whocaresleft.core.Id;

public class DoctorNotFoundException extends RuntimeException {
    
    private transient final Id doctorId;
    
    public DoctorNotFoundException(Id doctorId) {
        super("No doctor with id " + doctorId + " was found");
        this.doctorId = doctorId;
    }
    
    private static final long serialVersionUID = 1L;
    
    public Id getDoctorId() { return doctorId; }
}
