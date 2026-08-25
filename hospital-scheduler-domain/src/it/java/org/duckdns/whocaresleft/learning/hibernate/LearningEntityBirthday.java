package org.duckdns.whocaresleft.learning.hibernate;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity @Table(name = "learning_entitiy_birthdays")
public class LearningEntityBirthday {
    @Id @Column(name = "learning_entity_id")
    private String learningEntityId;
    private LocalDate dateOfBirth;
    
    @OneToOne @MapsId @JoinColumn(name = "learning_entity_id")
    private LearningEntity learningEntity;
    
    public LearningEntityBirthday() {}
    public LearningEntityBirthday(LearningEntity learningEntity, LocalDate dateOfBirth) {
        this.learningEntity = learningEntity;
        if (learningEntity != null) {
            this.learningEntityId = learningEntity.getId();
        }
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getLearningEntityId() { return learningEntityId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setLearningEntityId(String learningEntityId) { this.learningEntityId = learningEntityId; }
    public LearningEntity getLearningEntity() { return learningEntity; }
    public void setLearningEntity(LearningEntity learningEntity) { this.learningEntity = learningEntity; }
    
    
    @Override
    public int hashCode() {
        return Objects.hash(learningEntityId, dateOfBirth);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LearningEntityBirthday other = (LearningEntityBirthday) obj;
        return Objects.equals(learningEntityId, other.learningEntityId) && Objects.equals(dateOfBirth, other.dateOfBirth);
    }
    @Override
    public String toString() {
        return "LearningEntityBirthday [learningEntityId=" + learningEntityId + ", dateOfBirth=" + dateOfBirth + "]";
    }
}