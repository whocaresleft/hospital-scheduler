package org.duckdns.whocaresleft.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.function.IntPredicate;

import org.duckdns.whocaresleft.core.Id;

public final class Shift {

    private final Id workerId;
    private final Id departmentId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    
    private Shift(Id workerId, Id departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.workerId = workerId;
        this.departmentId = departmentId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
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
        
        return new Shift(workerId, departmentId, date, startTime, endTime);
    }
    
    public Id getWorkerId() { return workerId; }
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
        
        return Objects.equals(workerId, otherShift.workerId)
            && Objects.equals(departmentId, otherShift.departmentId)
            && Objects.equals(date, otherShift.date)
            && Objects.equals(startTime, otherShift.startTime)
            && Objects.equals(endTime, otherShift.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, departmentId, date, startTime, endTime);
    }
    
    @Override
    public String toString() {
        return "Shift [workerId=" + workerId + ", departmentId=" + departmentId + ", date=" + date + ", startTime="
                + startTime + ", endTime=" + endTime + "]";
    }

    public boolean overlaps(Shift second) {
        return true;
    }
}
