package org.duckdns.whocaresleft.repository.mariadb;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;

import jakarta.persistence.EntityManager;

public class MariaDoctorRepository implements DoctorRepository {

    private EntityManager entityManager;
    
    public MariaDoctorRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    public List<Doctor> findAll() {
        return entityManager.createQuery("SELECT e FROM DoctorEntity e", DoctorEntity.class)
            .getResultStream()
            .map(DoctorEntity::toDoctor)
            .toList();
    }

    @Override
    public Doctor findById(Id id) {
        return null;
    }

    @Override
    public void save(Doctor doctor) throws DuplicateDoctorException {
        
    }

    @Override
    public void delete(Id doctorId) throws DoctorNotFoundException {
        
    }

    @Override
    public void update(Id doctorId, Doctor newDoctor) throws DoctorNotFoundException {
        
    }

}
