package org.duckdns.whocaresleft.view;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Shift;

public interface ShiftView {
    
    void showAllShifts(List<Shift> shifts);
    void shiftAdded(Shift shift);
    void shiftRemoved(Shift shift);
    void shiftUpdated(Shift oldShift, Shift newShift);

    void showErrorOverlappedShift(Shift original, Shift overlapped);
    void showErrorShiftNotFound(Shift shift);
    void showErrorDoctorNotFound(Id doctorId);
    void showErrorDepartmentNotFound(Id departmentId);
}
