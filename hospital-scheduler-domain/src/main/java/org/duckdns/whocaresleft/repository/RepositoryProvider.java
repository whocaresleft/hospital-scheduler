package org.duckdns.whocaresleft.repository;

public interface RepositoryProvider {
    DoctorRepository getDoctorRepository();
    DepartmentRepository getDepartmentRepository();
    ShiftRepository getShiftRepository();
}
