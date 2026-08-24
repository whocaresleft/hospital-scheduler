package org.duckdns.whocaresleft.presenter;

import org.duckdns.whocaresleft.exception.DuplicateDoctorException;
import org.duckdns.whocaresleft.exception.DoctorNotFoundException;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.view.DoctorView;

public class DoctorPresenter {

    private DoctorRepository repository;
    private DoctorView view;
    
    public DoctorPresenter(DoctorRepository repository, DoctorView view) {
        this.repository = repository;
        this.view = view;
    }
    
    public void allDoctors() {
        view.showAllDoctors(repository.findAll());
    }

    public void addDoctor(Doctor doctor) {
        try {
            repository.save(doctor);
            view.doctorAdded(doctor);
        } catch (DuplicateDoctorException e) {
            view.showErrorDuplicateDoctor(e.getMessage(), doctor);
        }
    }

    public void removeDoctor(Doctor doctor) {
        try {
            repository.delete(doctor.getId());
            view.doctorRemoved(doctor);
        } catch (DoctorNotFoundException e) {
            view.showErrorDoctorNotFound(e.getMessage(), doctor);
        }
    }

    public void updateDoctor(Doctor oldDoctor, Doctor newDoctor) {
        try {
            repository.update(oldDoctor.getId(), newDoctor);
            view.doctorUpdated(oldDoctor, newDoctor);
        } catch (DoctorNotFoundException e) {
            view.showErrorDoctorNotFound(e.getMessage(), oldDoctor);
        }
    }
}
