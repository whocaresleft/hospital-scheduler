package org.duckdns.whocaresleft.repository.mariadb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import org.duckdns.whocaresleft.model.Shift;
import static org.duckdns.whocaresleft.core.Id.createId;

@Entity @Table(name = "shifts")
public class ShiftEntity {
    
    @Id @Column(name = "id")
    private String id;
    
    @Column(name = "doctor_id")
    private String doctorId;
    
    @Column(name = "department_id")
    private String departmentId;
    
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public ShiftEntity() {}
    public ShiftEntity(String id, String doctorId, String departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.doctorId = doctorId;
        this.departmentId = departmentId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getId() { return id; }
    public String getDoctorId() { return doctorId; }
    public String getDepartmentId() { return departmentId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    
    public void setId(String id) { this.id = id; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, date, departmentId, doctorId, endTime, startTime);
    }
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        
        if (other == null)
            return false;
        
        if (getClass() != other.getClass())
            return false;
        
        ShiftEntity otherShift = (ShiftEntity)other;
        return Objects.equals(id, otherShift.id)
            && Objects.equals(date, otherShift.date)
            && Objects.equals(departmentId, otherShift.departmentId)
            && Objects.equals(doctorId, otherShift.doctorId)
            && Objects.equals(endTime, otherShift.endTime)
            && Objects.equals(startTime, otherShift.startTime);
    }
    @Override
    public String toString() {
        return "ShiftEntity [id=" + id +", doctorId=" + doctorId + ", departmentId=" + departmentId + ", date=" + date
                + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }
    
    public Shift toShift() {
        return Shift.createShift(
            createId(doctorId),
            createId(departmentId),
            date,
            startTime,
            endTime);
    }
    
    public static ShiftEntity fromShift(Shift sh) {
        return new ShiftEntity(
            generateEntityId(sh),
            sh.getDoctorId().getValue(),
            sh.getDepartmentId().getValue(),
            sh.getDate(),
            sh.getStartTime(),
            sh.getEndTime());
    }
    
    public static String generateEntityId(Shift sh) {
        return String.format("%s-%s-%s-%s-%s",
            sh.getDoctorId().getValue(),
            sh.getDepartmentId().getValue(),
            sh.getDate().toString(),
            sh.getStartTime().toString(),
            sh.getEndTime().toString());
    }
}
