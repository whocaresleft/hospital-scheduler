package org.duckdns.whocaresleft.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import org.duckdns.whocaresleft.core.Id;

public class Shift {

    private Shift() {
        
        
    }
    
    public static Shift createShift(Id workerId, Id departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        
        if (workerId == null) 
            throw new IllegalArgumentException("Worker Id cannot be null");
        if (departmentId == null) 
            throw new IllegalArgumentException("Department Id cannot be null");
        if (date == null) 
            throw new IllegalArgumentException("Date cannot be null");
        if (startTime == null) 
            throw new IllegalArgumentException("Starting time cannot be null");
        if (endTime == null) 
            throw new IllegalArgumentException("Ending time cannot be null");
        
        if (startTime.equals(endTime)) 
            throw new IllegalArgumentException("Shift has zero duration, starting time equals ending time");
        if (startTime.isAfter(endTime))
            throw new IllegalArgumentException("Shift has negative duration, starting time is after than ending time");
        
        return null;
    }
}
