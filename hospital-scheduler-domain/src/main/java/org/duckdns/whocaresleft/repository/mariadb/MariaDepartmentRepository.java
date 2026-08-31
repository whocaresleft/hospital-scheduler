package org.duckdns.whocaresleft.repository.mariadb;

import java.util.List;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.exception.DepartmentNotFoundException;
import org.duckdns.whocaresleft.exception.DuplicateDepartmentException;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.repository.DepartmentRepository;
import org.duckdns.whocaresleft.repository.mariadb.entity.DepartmentEntity;

import jakarta.persistence.EntityManager;

public class MariaDepartmentRepository implements DepartmentRepository {
    
    private EntityManager entityManager;
    
    public MariaDepartmentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    public List<Department> findAll() {
        return entityManager.createQuery("SELECT e FROM DepartmentEntity e", DepartmentEntity.class)
            .getResultStream()
            .map(DepartmentEntity::toDepartment)
            .toList();
    }
    
    @Override
    public Department findById(Id id) {
        DepartmentEntity de = entityManager.find(DepartmentEntity.class, id.getValue());
        if (de == null)
            return null;
        return de.toDepartment();
    }
    
    @Override
    public void save(Department department) throws DuplicateDepartmentException {
        Department found = findById(department.getId());
        if (found != null)
            throw new DuplicateDepartmentException(found);
        entityManager.persist(DepartmentEntity.fromDepartment(department));
    }
    
    @Override
    public void delete(Id departmentId) throws DepartmentNotFoundException {
        DepartmentEntity de = entityManager.find(DepartmentEntity.class, departmentId.getValue());
        if (de == null)
            throw new DepartmentNotFoundException(departmentId);
        entityManager.remove(de);
    }
    
    @Override
    public void update(Id departmentId, Department newDepartment) throws DepartmentNotFoundException {
        DepartmentEntity de = entityManager.find(DepartmentEntity.class, departmentId.getValue());
        if (de == null)
            throw new DepartmentNotFoundException(departmentId);
        de.setName(newDepartment.getName());
    }

}
