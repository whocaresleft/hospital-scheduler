package org.duckdns.whocaresleft.repository.mariadb;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.OverlappedShiftException;
import org.duckdns.whocaresleft.exception.ShiftNotFoundException;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.repository.ShiftRepository;

import jakarta.persistence.EntityManager;

public class MariaShiftRepository implements ShiftRepository {
    
    private EntityManager entityManager;
    
    public MariaShiftRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    public List<Shift> findAll() {
        return null;
    }
    
    @Override
    public List<Shift> findByDoctorId(Id doctorId) {
        return null;
    }
    
    @Override
    public List<Shift> findByDepartmentId(Id departmentId) {
        return null;
    }
    
    @Override
    public void save(Shift shift) throws OverlappedShiftException {
        
    }
    
    @Override
    public void delete(Shift shift) throws ShiftNotFoundException {
        
    }
    
    @Override
    public void update(Shift oldShift, Shift newShift) throws ShiftNotFoundException, OverlappedShiftException {
        
    }
    
}
