package org.duckdns.whocaresleft.repository.mariadb;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.mariadb.entity.DoctorEntity;

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
        DoctorEntity doc = entityManager.find(DoctorEntity.class, id.getValue());
        if (doc != null)
            return doc.toDoctor();
        return null;
    }

    @Override
    public void save(Doctor doctor) throws DuplicateDoctorException {
        try {
            if (findById(doctor.getId()) != null)
                throw new DuplicateDoctorException(doctor);
            entityManager.persist(DoctorEntity.fromDoctor(doctor));
        } catch (Exception e) {
            throw new DuplicateDoctorException(doctor);
        }
    }

    @Override
    public void delete(Id doctorId) throws DoctorNotFoundException {
        DoctorEntity toBeRemoved = entityManager.find(DoctorEntity.class, doctorId.getValue());
        if (toBeRemoved == null) {
            throw new DoctorNotFoundException(doctorId);
        }
        entityManager.remove(toBeRemoved);
    }

    @Override
    public void update(Id doctorId, Doctor newDoctor) throws DoctorNotFoundException {
        DoctorEntity toBeUpdated = entityManager.find(DoctorEntity.class, doctorId.getValue());
        if (toBeUpdated == null) {
            throw new DoctorNotFoundException(doctorId);
        }
        toBeUpdated.setFirstName(newDoctor.getFirstName());
        toBeUpdated.setLastName(newDoctor.getLastName());
    }

}
