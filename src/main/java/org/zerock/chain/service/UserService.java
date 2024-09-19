package org.zerock.chain.service;

import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.EmployeeDTO;
import org.zerock.chain.model.Employee;
import org.zerock.chain.repository.EmployeeRepository;
import org.zerock.chain.model.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class UserService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    public UserService(EmployeeRepository employeeRepository, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    // 로그인한 사용자 정보 들고오기
    public EmployeeDTO getLoggedInUserDetails() {
        try {
            Long loggedInEmpNo = getLoggedInUserEmpNo();

            Employee employee = employeeRepository.findById(loggedInEmpNo)
                    .orElseThrow(() -> {
                        log.error("User with emp_no 1 not found");
                        return new RuntimeException("User not found");
                    });

            // 엔티티를 DTO로 변환
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setEmpNo(employee.getEmpNo());
            employeeDTO.setFirstName(employee.getFirstName());
            employeeDTO.setLastName(employee.getLastName());
            employeeDTO.setDepartmentId(employee.getDepartment().getDmpNo());
            employeeDTO.setRankId(employee.getRank().getRankNo());
            // 부서 이름과 직급 이름을 설정
            employeeDTO.setDepartmentName(employee.getDepartment().getDmpName());
            employeeDTO.setRankName(employee.getRank().getRankName());

            log.info("Successfully retrieved logged-in user details for emp_no {}", employee.getEmpNo());

            return employeeDTO;

        } catch (Exception e) {
            log.error("Error occurred while retrieving logged-in user details", e);
            throw e;
        }
    }

    // 모달창에서 결재선 설정하는 부분에 모든 직원의 정보가 필요하기에 모든 직원 정보 들고 오는 메서드
    public List<EmployeeDTO> getAllEmployees() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            return employees.stream()
                    .map(employee -> {
                        EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);

                        // rankName과 dmpName을 안전하게 설정
                        if (employee.getRank() != null) {
                            employeeDTO.setRankName(employee.getRank().getRankName());
                        } else {
                            log.warn("Employee with empNo {} has no rank", employee.getEmpNo());
                            employeeDTO.setRankName("Unknown");
                        }

                        if (employee.getDepartment() != null) {
                            employeeDTO.setDepartmentName(employee.getDepartment().getDmpName());
                        } else {
                            log.warn("Employee with empNo {} has no department", employee.getEmpNo());
                            employeeDTO.setDepartmentName("Unknown");
                        }

                        return employeeDTO;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error occurred while retrieving all employees", e);
            throw e;
        }
    }

    // 로그인한 사용자의 사원번호를 들고옴
    public Long getLoggedInUserEmpNo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getEmpNo();  // 사원 번호 반환
        } else {
            throw new RuntimeException("로그인된 사용자 정보를 가져올 수 없습니다.");
        }
    }
}
