package org.zerock.chain.service;


import org.zerock.chain.model.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Department createDepartment(Department department);
    Department updateDepartment(Long id, Department department);
    void deleteDepartment(Long id);
}