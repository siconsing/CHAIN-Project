package org.zerock.chain.imjongha.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.imjongha.dto.DepartmentDTO;
import org.zerock.chain.imjongha.dto.EmployeeDTO;
import org.zerock.chain.imjongha.model.Department;
import org.zerock.chain.imjongha.model.Employee;
import org.zerock.chain.imjongha.repository.DepartmentRepository;
import org.zerock.chain.imjongha.repository.EmployeeRepository;
import org.zerock.chain.imjongha.service.EmployeeService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public DepartmentController(DepartmentRepository departmentRepository, EmployeeService employeeService, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }

    // 모든 부서 조회
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departmentDTOs = departmentRepository.findAll().stream()
                .map(department -> {
                    DepartmentDTO dto = new DepartmentDTO();
                    dto.setDmpNo(department.getDmpNo());
                    dto.setDmpName(department.getDmpName());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(departmentDTOs);
    }

    // 조직도 데이터를 제공하는 API 엔드포인트
    @GetMapping("/organization")
    public ResponseEntity<List<DepartmentDTO>> getOrganizationStructure() {
        List<DepartmentDTO> departmentDTOs = departmentRepository.findAll().stream()
                .map(department -> new DepartmentDTO(
                        department.getDmpNo(),
                        department.getDmpName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(departmentDTOs);
    }

    // 부서별 사원 목록 조회 추가
    @GetMapping("/{departmentId}/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable("departmentId") Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartmentId(departmentId);
        if (employees.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employees);
    }

    // 부서 추가/수정
    @PostMapping
    public ResponseEntity<DepartmentDTO> createOrUpdateDepartment(@RequestBody DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setDmpNo(departmentDTO.getDmpNo());
        department.setDmpName(departmentDTO.getDmpName());
        Department savedDepartment = departmentRepository.save(department);

        DepartmentDTO savedDepartmentDTO = new DepartmentDTO();
        savedDepartmentDTO.setDmpNo(savedDepartment.getDmpNo());
        savedDepartmentDTO.setDmpName(savedDepartment.getDmpName());

        return new ResponseEntity<>(savedDepartmentDTO, HttpStatus.CREATED);
    }

    // 부서 삭제 처리
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Void> deleteDepartments(@RequestBody Map<String, List<Long>> departmentIdsMap) {
        List<Long> departmentIds = departmentIdsMap.get("departmentIds");

        // 사원들의 부서를 null로 설정
        for (Long departmentId : departmentIds) {
            List<Employee> employees = employeeRepository.findByDepartmentDmpNo(departmentId);
            for (Employee employee : employees) {
                employee.setDepartment(null);
                employeeRepository.save(employee);
            }
        }

        // 부서 삭제
        departmentRepository.deleteAllById(departmentIds);
        return ResponseEntity.noContent().build();
    }
}
