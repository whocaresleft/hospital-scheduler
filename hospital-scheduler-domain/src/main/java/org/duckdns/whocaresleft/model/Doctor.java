package org.duckdns.whocaresleft.model;
import java.util.Objects;

import org.duckdns.whocaresleft.core.Id;

public final class Doctor {
    
    private final Id id;
    private final String firstName;
    private final String lastName;
    
    private Doctor(Id id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public static Doctor createDoctor(Id id, String firstName, String lastName) {
        return new Doctor(id, firstName, lastName);
    }
    
    public Id getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, id, lastName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        Doctor other = (Doctor) obj;
        return Objects.equals(firstName, other.firstName)
            && Objects.equals(id, other.id)
            && Objects.equals(lastName, other.lastName);
    }

    @Override
    public String toString() {
        return "Doctor [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }
    

}
