package org.duckdns.whocaresleft.repository.mariadb;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

import jakarta.persistence.Id;
import org.duckdns.whocaresleft.model.Doctor;

@Entity @Table(name = "doctors")
public class DoctorEntity {

    @Id private String id;
    private String firstName;
    private String lastName;
    
    public DoctorEntity() {}
    public DoctorEntity(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    
    public void setId(String id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    @Override
    public int hashCode() { return Objects.hash(id, firstName, lastName); }
    
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        
        if (other == null)
            return false;
        
        if (getClass() != other.getClass())
            return false;
        
        DoctorEntity otherDoc = (DoctorEntity)other;
        return Objects.equals(id, otherDoc.id)
            && Objects.equals(firstName, otherDoc.firstName)
            && Objects.equals(lastName, otherDoc.lastName);
    }
    @Override
    public String toString() {
        return "DoctorEntity [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }
    
    public Doctor toDoctor() {
        return Doctor.createDoctor(
            org.duckdns.whocaresleft.core.Id.createId(id),
            firstName,
            lastName);
    }
    
    public static DoctorEntity fromDoctor(Doctor doc) {
        return new DoctorEntity(doc.getId().getValue(), doc.getFirstName(), doc.getLastName());
    }
}
