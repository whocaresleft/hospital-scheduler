package org.duckdns.whocaresleft.presenter;

import org.duckdns.whocaresleft.exception.DuplicateDoctorException;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.view.DoctorView;

public class DoctorPresenter {
    
    private static final Logger LOGGER = LogManager.getLogger(DoctorPresenter.class);
    
    private DoctorRepository repository;
    private DoctorView view;
    
    public DoctorPresenter(DoctorRepository repository, DoctorView view) {
        this.repository = repository;
        this.view = view;
    }
    
    public void allDoctors() {
        List<Doctor> doctors = repository.findAll();
        LOGGER.debug("Retrieved {} doctors from repository.", doctors.size());
        view.showAllDoctors(doctors);
    }
    
    public void addDoctor(Doctor doctor) {
        try {
            repository.save(doctor);
            LOGGER.debug("Doctor {} was saved to database", doctor);
            view.doctorAdded(doctor);
        } catch (DuplicateDoctorException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDuplicateDoctor(doctor.getId());
        }
    }
    
    public void removeDoctor(Doctor doctor) {
        try {
            repository.delete(doctor.getId());
            LOGGER.debug("Doctor {} was deleted from database", doctor);
            view.doctorRemoved(doctor);
        } catch (DoctorNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDoctorNotFound(doctor.getId());
        }
    }
    
    public void updateDoctor(Doctor oldDoctor, Doctor newDoctor) {
        try {
            repository.update(oldDoctor.getId(), newDoctor);
            LOGGER.debug("Doctor {} was updated into {}", oldDoctor, newDoctor);
            view.doctorUpdated(oldDoctor, newDoctor);
        } catch (DoctorNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDoctorNotFound(oldDoctor.getId());
        }
    }
}
