package org.duckdns.whocaresleft.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import org.duckdns.whocaresleft.core.Id;

public class Shift {

    private Shift() {
        
        
    }
    
    public static Shift createShift(Id workerId, Id departmentId, LocalDate date, LocalTime start, LocalTime end) {
        
        if (workerId == null) throw new IllegalArgumentException("Worker Id cannot be null");
        if (departmentId == null) throw new IllegalArgumentException("Department Id cannot be null");
        if (date == null) throw new IllegalArgumentException("Date cannot be null");
        if (start == null) throw new IllegalArgumentException("Starting time cannot be null");
        if (end == null) throw new IllegalArgumentException("Ending time cannot be null");
        
        return null;
    }
}
