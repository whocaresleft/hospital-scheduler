package org.duckdns.whocaresleft.view.swing;

import java.util.List;

import javax.swing.JPanel;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.view.DoctorView;

public class SwingDoctorView extends JPanel implements DoctorView {
    public SwingDoctorView() {
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void showAllDoctors(List<Doctor> doctors) {
        
    }

    @Override
    public void showSingleDoctor(Doctor doctor) {
        
    }

    @Override
    public void doctorAdded(Doctor doctor) {
        
    }

    @Override
    public void doctorRemoved(Doctor doctor) {
        
    }

    @Override
    public void doctorUpdated(Doctor oldDoctor, Doctor newDoctor) {
        
    }

    @Override
    public void showErrorDuplicateDoctor(Id duplicated) {
        
    }

    @Override
    public void showErrorDoctorNotFound(Id notFound) {
        
    }
    
}
