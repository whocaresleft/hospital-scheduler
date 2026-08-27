package org.duckdns.whocaresleft.presenter;

import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.view.DepartmentView;

public class DepartmentPresenter {
    
    private DepartmentRepository repository;
    private DepartmentView view;
    
    public DepartmentPresenter(DepartmentRepository repository, DepartmentView view) {
        this.repository = repository;
        this.view = view;
    }
    
    public void allDepartments() {
        view.showAllDepartments(repository.findAll());
    }
    
    public void addDepartment(Department department) {
        try {
            repository.save(department);
            view.departmentAdded(department);
        } catch (DuplicateDepartmentException e) {
            view.showDuplicateDepartmentError(department.getId());
        }
    }
    
    public void removeDepartment(Department department) {
        try {
            repository.delete(department.getId());
            view.departmentRemoved(department);
        } catch (DepartmentNotFoundException e) {
            view.showDepartmentNotFoundError(department.getId());
        }
    }
    
    public void updateDepartment(Department oldDepartment, Department newDepartment) {
        try {
            repository.update(oldDepartment.getId(), newDepartment);
            view.departmentUpdated(oldDepartment, newDepartment);
        } catch (DepartmentNotFoundException e) {
            view.showDepartmentNotFoundError(oldDepartment.getId());
        }
    }
}
