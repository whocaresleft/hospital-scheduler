package org.duckdns.whocaresleft.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import org.duckdns.whocaresleft.core.Id;

public final class Shift {

    private final Id doctorId;
    private final Id departmentId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    
    private Shift(Id doctorId, Id departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.doctorId = doctorId;
        this.departmentId = departmentId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public static Shift createShift(Id doctorId, Id departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        
        if (doctorId == null) 
            throw new IllegalArgumentException("Doctor Id cannot be null");
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
        
        return new Shift(doctorId, departmentId, date, startTime, endTime);
    }
    
    public Id getDoctorId() { return doctorId; }
    public Id getDepartmentId() { return departmentId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        
        if (other == null)
            return false;
        
        if (getClass() != other.getClass())
            return false;
        
        Shift otherShift = (Shift)other;
        
        return Objects.equals(doctorId, otherShift.doctorId)
            && Objects.equals(departmentId, otherShift.departmentId)
            && Objects.equals(date, otherShift.date)
            && Objects.equals(startTime, otherShift.startTime)
            && Objects.equals(endTime, otherShift.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId, departmentId, date, startTime, endTime);
    }
    
    @Override
    public String toString() {
        return "(" + doctorId + "-" + departmentId + "), " + date + ": (" + startTime + "-" + endTime +")";
    }

    public boolean overlaps(Shift other) {
        return date.equals(other.date)
            && endTime.isAfter(other.startTime) 
            && startTime.isBefore(other.endTime);
    }
}
