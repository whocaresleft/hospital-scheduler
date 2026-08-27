package org.duckdns.whocaresleft.repository;

import java.util.List;

import org.duckdns.whocaresleft.model.Department;

public interface DepartmentRepository {

    public List<Department> findAll();

}
