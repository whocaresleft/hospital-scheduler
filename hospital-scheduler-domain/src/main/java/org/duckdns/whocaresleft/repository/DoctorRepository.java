package org.duckdns.whocaresleft.repository;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.model.Doctor;

public interface DoctorRepository {

    public List<Doctor> findAll();
    public void save(Doctor doctor) throws DuplicateDoctorException;
    public void delete(Id doctorId) throws DoctorNotFoundException;
    public void update(Id doctorId, Doctor newDoctor) throws DoctorNotFoundException;
}
