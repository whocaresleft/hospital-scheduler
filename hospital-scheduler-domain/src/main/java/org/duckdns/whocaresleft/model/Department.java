package org.duckdns.whocaresleft.model;
import java.util.Objects;

import org.duckdns.whocaresleft.core.Id;

public final class Department {
    
    private final Id id;
    private final String name;
    
    private Department(Id id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public static Department createDepartment(Id id, String name) {
        return new Department(id, name);
    }
    
    public Id getId() { return id; }
    public String getName() { return name; }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        Department other = (Department) obj;
        return Objects.equals(id, other.id)
            && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return "Department [id=" + id + ", name=" + name + "]";
    }
    

}
