package org.duckdns.whocaresleft.presenter;

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

}
