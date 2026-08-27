package org.duckdns.whocaresleft.presenter;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.transactions.TransactionManager;
import org.duckdns.whocaresleft.view.DepartmentView;

public class DepartmentPresenter {
    
    private static final Logger LOGGER = LogManager.getLogger(DepartmentPresenter.class);

    private TransactionManager transactionManager;
    private DepartmentView view;
    
    public DepartmentPresenter(TransactionManager transactionManager, DepartmentView view) {
        this.transactionManager = transactionManager;
        this.view = view;
    }
    
    public void allDepartments() {
        List<Department> departments = transactionManager.doInTransaction(repositoryProvider -> {
            DepartmentRepository repository = repositoryProvider.getDepartmentRepository();
            return repository.findAll();
        });
        LOGGER.debug("Retrieved {} departments from repository.", departments.size());
        view.showAllDepartments(departments);
    }
    
    public void addDepartment(Department department) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                DepartmentRepository repository = repositoryProvider.getDepartmentRepository();
                repository.save(department);
                return null;
            });
            LOGGER.debug("Department {} was saved to repository", department);
            view.departmentAdded(department);
        } catch (DuplicateDepartmentException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showDuplicateDepartmentError(department.getId());
        }
    }
    
    public void removeDepartment(Department department) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                DepartmentRepository repository = repositoryProvider.getDepartmentRepository();
                repository.delete(department.getId());
                return null;
            });
            LOGGER.debug("Department {} was deleted from repository", department);
            view.departmentRemoved(department);
        } catch (DepartmentNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showDepartmentNotFoundError(department.getId());
        }
    }
    
    public void updateDepartment(Department oldDepartment, Department newDepartment) {
        try {
            transactionManager.doInTransaction(repositoryProvider -> {
                DepartmentRepository repository = repositoryProvider.getDepartmentRepository();
                repository.update(oldDepartment.getId(), newDepartment);
                return null;
            });
            LOGGER.debug("Department {} was updated into {}", oldDepartment, newDepartment);
            view.departmentUpdated(oldDepartment, newDepartment);
        } catch (DepartmentNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            view.showDepartmentNotFoundError(oldDepartment.getId());
        }
    }
}
