package org.duckdns.whocaresleft.view;

import java.util.List;

import org.duckdns.whocaresleft.model.Doctor;

public interface DoctorView {

    void showAllDoctors(List<Doctor> doctors);
}
