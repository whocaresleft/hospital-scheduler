package org.duckdns.whocaresleft.learning.hibernate;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "learning_entities")
public class LearningEntity {
    @Id private String id;
    private String name;
    
    public LearningEntity() {}
    public LearningEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public void setId(String id) { this.id = id; }
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
        LearningEntity other = (LearningEntity) obj;
        return Objects.equals(id, other.id) && Objects.equals(name, other.name);
    }
    @Override
    public String toString() {
        return "LearningEntity [id=" + id + ", name=" + name + "]";
    }
}