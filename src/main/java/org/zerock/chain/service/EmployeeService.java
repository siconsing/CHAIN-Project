package org.zerock.chain.service;

import org.springframework.data.domain.Page;
import org.zerock.chain.dto.EmployeeDTO;

import java.util.List;

public interface EmployeeService {
    List<EmployeeDTO> getAllEmployees();
    EmployeeDTO getEmployeeById(Long empNo);
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long empNo, EmployeeDTO employeeDTO);
    void deleteEmployee(Long empNo);
    List<EmployeeDTO> searchEmployees(String name, String departmentName, String rankName);

    Page<EmployeeDTO> getEmployeesPaged(int page, int size);
    List<EmployeeDTO> getEmployeesByDepartmentId(Long departmentId);
//
//
    // 박성은 추가 코드
    List<EmployeeDTO> getAllEmployeesExcept(Long loggedInEmpNo); // 로그인 사원번호 제외 조직도 구현
}