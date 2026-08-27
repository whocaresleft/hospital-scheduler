package org.duckdns.whocaresleft.repository.mariadb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

import jakarta.persistence.Id;
import org.duckdns.whocaresleft.model.Department;

@Entity @Table(name = "departments")
public class DepartmentEntity {

    @Id private String id;
    private String name;
    
    public DepartmentEntity() {}
    public DepartmentEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    
    @Override
    public int hashCode() { return Objects.hash(id, name); }
    
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        
        if (other == null)
            return false;
        
        if (getClass() != other.getClass())
            return false;
        
        DepartmentEntity otherDep = (DepartmentEntity)other;
        return Objects.equals(id, otherDep.id)
            && Objects.equals(name, otherDep.name);
    }
    @Override
    public String toString() {
        return "DepartmentEntity [id=" + id + ", name=" + name + "]";
    }
    
    public Department toDepartment() {
        return Department.createDepartment(
            org.duckdns.whocaresleft.core.Id.createId(id),
            name);
    }
    
    public static DepartmentEntity fromDepartment(Department dep) {
        return new DepartmentEntity(dep.getId().getValue(), dep.getName());
    }
}
