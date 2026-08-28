package org.duckdns.whocaresleft.repository.mariadb;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.OverlappedShiftException;
import org.duckdns.whocaresleft.exception.ShiftNotFoundException;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.repository.mariadb.entity.ShiftEntity;

import jakarta.persistence.EntityManager;

public class MariaShiftRepository implements ShiftRepository {
    
    private EntityManager entityManager;
    
    public MariaShiftRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    public List<Shift> findAll() {
        return entityManager.createQuery("SELECT e FROM ShiftEntity e", ShiftEntity.class)
            .getResultStream()
            .map(ShiftEntity::toShift)
            .toList();
    }
    
    @Override
    public List<Shift> findByDoctorId(Id doctorId) {
        return entityManager.createQuery(
                "SELECT e FROM ShiftEntity e WHERE e.doctorId = :doctorId", ShiftEntity.class)
            .setParameter("doctorId", doctorId.getValue())
            .getResultStream()
            .map(ShiftEntity::toShift)
            .toList();
    }
    
    @Override
    public List<Shift> findByDepartmentId(Id departmentId) {
        return entityManager.createQuery(
                "SELECT e FROM ShiftEntity e WHERE e.departmentId = :departmentId", ShiftEntity.class)
            .setParameter("departmentId", departmentId.getValue())
            .getResultStream()
            .map(ShiftEntity::toShift)
            .toList();
    }
    
    @Override
    public void save(Shift shift) throws OverlappedShiftException {
        if (entityManager.find(ShiftEntity.class, ShiftEntity.generateEntityId(shift)) != null)
            throw new OverlappedShiftException(shift, shift);
        entityManager.persist(ShiftEntity.fromShift(shift));
    }
    
    @Override
    public void delete(Shift shift) throws ShiftNotFoundException {
        ShiftEntity toDelete = entityManager.find(ShiftEntity.class, ShiftEntity.generateEntityId(shift));
        if (toDelete == null)
            throw new ShiftNotFoundException(shift);
        entityManager.remove(toDelete);
    }
    
    @Override
    public void update(Shift oldShift, Shift newShift) throws ShiftNotFoundException, OverlappedShiftException {
        delete(oldShift);
        save(newShift);
    }
}
