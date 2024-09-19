package org.zerock.chain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.dto.EmployeeDTO;
import org.zerock.chain.service.EmployeeService;
import org.zerock.chain.model.CustomUserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // 인증된 사용자 정보 가져오기
    private CustomUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
        }
        return null;
    }

    // 모든 직원 조회
    @GetMapping("/all")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        System.out.println("Employees fetched: " + employees); // 서버에서 응답 직전에 데이터 출력
        return ResponseEntity.ok(employees);
    }

    // ID로 특정 직원 조회 (명시적으로 "id" 이름 지정)
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable("id") Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    // 직원 생성
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    // ID로 특정 직원 정보 업데이트 (명시적으로 "id" 이름 지정)
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable("id") Long id, @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    // ID로 특정 직원 삭제 (명시적으로 "id" 이름 지정)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
//
//    // 박성은 추가 코드
//    @GetMapping("/employee-info")
//    public ResponseEntity<Map<String, String>> employeeInfo(HttpServletRequest request) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Map<String, String> response = new HashMap<>();
//
//        if (authentication != null && authentication.isAuthenticated()) {
//            Object principal = authentication.getPrincipal();
//            response.put("username", authentication.getName()); // 사원이름
//
//            // 세션에서 사원번호 가져오기
//            Object empNo = request.getSession().getAttribute("empNo");
//            if (empNo != null) {
//                response.put("empNo", empNo.toString());
//            } else {
//                response.put("empNo", "N/A");
//            }
//        } else {
//            response.put("empNo", "N/A");
//            response.put("username", "N/A");
//        }
//
//        return ResponseEntity.ok(response);
//    }

    // 로그인한 사원번호를 제외한 전체 사원 목록 조회
    @GetMapping("/all-except-logged-in")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployeesExceptLoggedIn() {
        CustomUserDetails loggedInUser = getAuthenticatedUser();
        Long loggedInEmpNo = loggedInUser != null ? loggedInUser.getEmpNo() : null;

        List<EmployeeDTO> employees = employeeService.getAllEmployeesExcept(loggedInEmpNo);
        return ResponseEntity.ok(employees);
    }
}
