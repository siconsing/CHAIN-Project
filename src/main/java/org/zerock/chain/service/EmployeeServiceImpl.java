package org.zerock.chain.service;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.EmployeeDTO;
import org.zerock.chain.model.Department;
import org.zerock.chain.model.EmpRank;
import org.zerock.chain.model.Employee;
import org.zerock.chain.repository.DepartmentRepository;
import org.zerock.chain.repository.EmployeeRepository;
import org.zerock.chain.repository.RankRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RankRepository rankRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, RankRepository rankRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.rankRepository = rankRepository;
    }

//   사원 목록 전체 조회
    @Override
    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeById(Long empNo) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("사원을 찾을 수 없습니다."));
        return convertToDTO(employee);
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        Employee employee = convertToEntity(employeeDTO);
        employee.setHireDate(LocalDate.now());
        Employee savedEmployee = employeeRepository.save(employee);

        return convertToDTO(savedEmployee);
    }

    @Override
    public EmployeeDTO updateEmployee(Long empNo, EmployeeDTO employeeDTO) {
        Employee existingEmployee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("사원을 찾을 수 없습니다."));

        existingEmployee.setFirstName(employeeDTO.getFirstName());
        existingEmployee.setLastName(employeeDTO.getLastName());
        existingEmployee.setPhoneNum(employeeDTO.getPhoneNum());
        existingEmployee.setBirthDate(employeeDTO.getBirthDate());
        existingEmployee.setAddr(employeeDTO.getAddr());
//        existingEmployee.setEmail(employeeDTO.getEmail());
        existingEmployee.setLastDate(employeeDTO.getLastDate());

        Department department = departmentRepository.findById(employeeDTO.getDepartmentNo())
                .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다."));
        existingEmployee.setDepartment(department);

         EmpRank rank = rankRepository.findById(employeeDTO.getRankNo())
                .orElseThrow(() -> new EntityNotFoundException("직급을 찾을 수 없습니다."));
        existingEmployee.setRank(rank);

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return convertToDTO(updatedEmployee);
    }


    @Override
    public void deleteEmployee(Long empNo) {
        employeeRepository.deleteById(empNo);
    }

    @Override
    public List<EmployeeDTO> searchEmployees(String name, String departmentName, String rankName) {
        List<Employee> employees = employeeRepository.findEmployeesByCriteria(name, departmentName, rankName);
        return employees.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<EmployeeDTO> getEmployeesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);
        return employeePage.map(this::convertToDTO);
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmpNo(employee.getEmpNo());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setPhoneNum(employee.getPhoneNum());
        dto.setBirthDate(employee.getBirthDate());
        dto.setAddr(employee.getAddr());
//        dto.setEmail(employee.getEmail());
        dto.setHireDate(employee.getHireDate());
        dto.setLastDate(employee.getLastDate());
        dto.setDepartmentNo(employee.getDepartment().getDmpNo());
        dto.setDmpName(employee.getDepartment().getDmpName());
        dto.setRankNo(employee.getRank().getRankNo());
        dto.setRankName(employee.getRank().getRankName());
        return dto;
    }


    private Employee convertToEntity(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setPhoneNum(dto.getPhoneNum());
        employee.setBirthDate(dto.getBirthDate());
        employee.setAddr(dto.getAddr());
//        employee.setEmail(dto.getEmail());

        if(dto.getDepartmentNo() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentNo())
                    .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다."));
            employee.setDepartment(department);
        }

        if(dto.getRankNo() != null) {
            EmpRank rank = rankRepository.findById(dto.getRankNo()).orElseThrow(() -> new EntityNotFoundException("직급을 찾을 수 없습니다."));
            employee.setRank(rank);
        }

        return employee;
    }
}