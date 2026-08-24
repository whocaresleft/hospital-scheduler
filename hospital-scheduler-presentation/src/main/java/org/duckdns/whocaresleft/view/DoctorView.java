package org.duckdns.whocaresleft.view;

import java.util.List;

import org.duckdns.whocaresleft.model.Doctor;

public interface DoctorView {

    void showAllDoctors(List<Doctor> doctors);
    void doctorAdded(Doctor doctor);
    void doctorRemoved(Doctor doctor);
    void doctorUpdated(Doctor oldDoctor, Doctor newDoctor);
    
    void showErrorDuplicateDoctor(String string, Doctor duplicated);
    void showErrorDoctorNotFound(String string, Doctor notFound);
}
