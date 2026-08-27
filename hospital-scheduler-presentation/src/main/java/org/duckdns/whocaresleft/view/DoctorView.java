package org.duckdns.whocaresleft.view;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;

public interface DoctorView {
    
    void showAllDoctors(List<Doctor> doctors);
    void doctorAdded(Doctor doctor);
    void doctorRemoved(Doctor doctor);
    void doctorUpdated(Doctor oldDoctor, Doctor newDoctor);
    
    void showErrorDuplicateDoctor(Id duplicated);
    void showErrorDoctorNotFound(Id notFound);
}
