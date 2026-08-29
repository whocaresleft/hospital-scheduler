package org.duckdns.whocaresleft.presenter;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.ShiftRepository;
import org.duckdns.whocaresleft.transaction.TransactionManager;
import org.duckdns.whocaresleft.view.DoctorView;

public class DoctorPresenter {
    
    private static final Logger LOGGER = LogManager.getLogger(DoctorPresenter.class);
    
    private TransactionManager transactionManager;
    private DoctorView view;
    
    public DoctorPresenter(TransactionManager transactionManager, DoctorView view) {
        this.transactionManager = transactionManager;
        this.view = view;
    }
    
    public void allDoctors() {
        List<Doctor> doctors = transactionManager.doInTransaction(repositoryProvider -> {
            DoctorRepository repository = repositoryProvider.getDoctorRepository();
            return repository.findAll();
        });
        LOGGER.debug("Retrieved {} doctors from repository.", doctors.size());
        view.showAllDoctors(doctors);
    }
    
    public void addDoctor(Doctor doctor) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                DoctorRepository repository = repositoryProvider.getDoctorRepository();
                repository.save(doctor);
                return null;
            });
            LOGGER.debug("Doctor {} was saved to repository", doctor);
            view.doctorAdded(doctor);
        } catch (DuplicateDoctorException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDuplicateDoctor(doctor.getId());
        }
    }
    
    public void removeDoctor(Doctor doctor) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                DoctorRepository doctorRepository = repositoryProvider.getDoctorRepository();
                ShiftRepository shiftRepository = repositoryProvider.getShiftRepository();
                
                shiftRepository.findByDoctorId(doctor.getId())
                    .forEach(shiftRepository::delete);
                
                doctorRepository.delete(doctor.getId());
                return null;
            });
            LOGGER.debug("Doctor {} was deleted from repository", doctor);
            view.doctorRemoved(doctor);
        } catch (DoctorNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDoctorNotFound(doctor.getId());
        }
    }
    
    public void updateDoctor(Doctor oldDoctor, Doctor newDoctor) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                DoctorRepository repository = repositoryProvider.getDoctorRepository();
                repository.update(oldDoctor.getId(), newDoctor);
                return null;
            });
            LOGGER.debug("Doctor {} was updated into {}", oldDoctor, newDoctor);
            view.doctorUpdated(oldDoctor, newDoctor);
        } catch (DoctorNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showErrorDoctorNotFound(oldDoctor.getId());
        }
    }
}
