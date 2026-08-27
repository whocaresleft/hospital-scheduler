package org.duckdns.whocaresleft.presenter;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.view.DepartmentView;

public class DepartmentPresenter {
    
    private static final Logger LOGGER = LogManager.getLogger(DepartmentPresenter.class);
    
    private DepartmentRepository repository;
    private DepartmentView view;
    
    public DepartmentPresenter(DepartmentRepository repository, DepartmentView view) {
        this.repository = repository;
        this.view = view;
    }
    
    public void allDepartments() {
        List<Department> departments = repository.findAll();
        LOGGER.debug("Retrieved {} departments from repository.", departments.size());
        view.showAllDepartments(departments);
    }
    
    public void addDepartment(Department department) {
        try {
            repository.save(department);
            LOGGER.debug("Department {} was saved to repository", department);
            view.departmentAdded(department);
        } catch (DuplicateDepartmentException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showDuplicateDepartmentError(department.getId());
        }
    }
    
    public void removeDepartment(Department department) {
        try {
            repository.delete(department.getId());
            LOGGER.debug("Department {} was deleted from repository", department);
            view.departmentRemoved(department);
        } catch (DepartmentNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showDepartmentNotFoundError(department.getId());
        }
    }
    
    public void updateDepartment(Department oldDepartment, Department newDepartment) {
        try {
            repository.update(oldDepartment.getId(), newDepartment);
            LOGGER.debug("Department {} was updated into {}", oldDepartment, newDepartment);
            view.departmentUpdated(oldDepartment, newDepartment);
        } catch (DepartmentNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showDepartmentNotFoundError(oldDepartment.getId());
        }
    }
}
