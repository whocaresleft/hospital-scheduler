package org.duckdns.whocaresleft.repository;

import java.util.List;

import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;

public interface DoctorRepository {

    public List<Doctor> findAll();
    public void save(Doctor doctor) throws DuplicateDoctorException;
}
