package org.duckdns.whocaresleft.repository;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.ShiftNotFoundException;
import org.duckdns.whocaresleft.model.Shift;

public interface ShiftRepository {
    
    List<Shift> findAll();
    List<Shift> findByDoctorId(Id doctorId);
    void save(Shift shift);
    void delete(Shift shift) throws ShiftNotFoundException;
    void update(Shift oldShift, Shift newShift) throws ShiftNotFoundException;
}
