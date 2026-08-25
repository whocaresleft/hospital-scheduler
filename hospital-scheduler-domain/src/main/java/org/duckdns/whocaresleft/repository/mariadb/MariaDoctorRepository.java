package org.duckdns.whocaresleft.repository.mariadb;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;

public class MariaDoctorRepository implements DoctorRepository {

    public MariaDoctorRepository() {
        
    }
    
    @Override
    public List<Doctor> findAll() {
        return null;
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
