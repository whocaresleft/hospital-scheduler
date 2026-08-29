package org.duckdns.whocaresleft.repository.mariadb;

import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.DoctorRepository;
import org.duckdns.whocaresleft.repository.RepositoryProvider;
import org.duckdns.whocaresleft.repository.ShiftRepository;

import jakarta.persistence.EntityManager;

public class MariaRepositoryProvider implements RepositoryProvider {
    
    private EntityManager entityManager;
    
    public MariaRepositoryProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    public DoctorRepository getDoctorRepository() {
        return new MariaDoctorRepository(entityManager);
    }
    
    @Override
    public DepartmentRepository getDepartmentRepository() {
        return new MariaDepartmentRepository(entityManager);
    }
    
    @Override
    public ShiftRepository getShiftRepository() {
        return new MariaShiftRepository(entityManager);
    }
}
