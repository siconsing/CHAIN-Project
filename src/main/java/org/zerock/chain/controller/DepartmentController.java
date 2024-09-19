package org.zerock.chain.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.dto.DepartmentDTO;
import org.zerock.chain.dto.EmployeeDTO;
import org.zerock.chain.model.Department;
import org.zerock.chain.model.Employee;
import org.zerock.chain.repository.DepartmentRepository;
import org.zerock.chain.repository.EmployeeRepository;
import org.zerock.chain.service.EmployeeService;

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
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            return ResponseEntity.noContent().build();  // 데이터가 없을 때 204 응답
        }

        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(department -> new DepartmentDTO(department.getDmpNo(), department.getDmpName()))
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

    // DepartmentController.java
    @GetMapping("/{departmentId}/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable("departmentId") Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 부서가 없을 경우 404 반환
        }

        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartmentId(departmentId);
        if (employees.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 사원이 없을 경우 204 반환
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
