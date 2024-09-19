package org.zerock.chain.service;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.dto.EmployeeDTO;
import org.zerock.chain.exception.EmployeeNotFoundException;
import org.zerock.chain.model.Department;
import org.zerock.chain.model.Employee;
import org.zerock.chain.model.Rank;
import org.zerock.chain.repository.DepartmentRepository;
import org.zerock.chain.repository.EmployeeRepository;
import org.zerock.chain.repository.RankRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RankRepository rankRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               RankRepository rankRepository,
                               ModelMapper modelMapper, NotificationService notificationService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.rankRepository = rankRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeById(Long empNo) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EntityNotFoundException("사원을 찾을 수 없습니다. ID: " + empNo));
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
                .orElseThrow(() -> new EntityNotFoundException("사원을 찾을 수 없습니다. ID: " + empNo));

        // 기존 부서와 직급 정보를 저장합니다.
        String oldDepartmentName = existingEmployee.getDepartment() != null ? existingEmployee.getDepartment().getDmpName() : null;
        String oldRankName = existingEmployee.getRank() != null ? existingEmployee.getRank().getRankName() : null;

        // 사원 정보를 업데이트합니다.
        updateEmployeeFromDTO(existingEmployee, employeeDTO);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);

        // 부서 및 직급 변경 알림을 생성합니다.
        String newDepartmentName = updatedEmployee.getDepartment() != null ? updatedEmployee.getDepartment().getDmpName() : null;
        String newRankName = updatedEmployee.getRank() != null ? updatedEmployee.getRank().getRankName() : null;

        notificationService.createDepartmentAndRankChangeNotification(
                empNo,
                oldDepartmentName,
                newDepartmentName,
                oldRankName,
                newRankName
        );

        return convertToDTO(updatedEmployee);
    }



    @Transactional
    public void deleteEmployee(Long empNo) {
        if (!employeeRepository.existsById(empNo)) {
            throw new EmployeeNotFoundException("사원을 찾을 수 없습니다. ID: " + empNo);
        }
        employeeRepository.deleteById(empNo);
    }


    @Override
    public List<EmployeeDTO> searchEmployees(String name, String departmentName, String rankName) {
        return employeeRepository.findEmployeesByCriteria(name, departmentName, rankName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EmployeeDTO> getEmployeesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public List<EmployeeDTO> getEmployeesByDepartmentId(Long departmentId) {
        return employeeRepository.findByDepartmentDmpNo(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }




    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = modelMapper.map(employee, EmployeeDTO.class);
        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getDmpNo());
            dto.setDepartmentName(employee.getDepartment().getDmpName());
        }
        if (employee.getRank() != null) {
            dto.setRankId(employee.getRank().getRankNo());
            dto.setRankName(employee.getRank().getRankName());
        }
        dto.setPermissionIds(employee.getEmployeePermissions().stream()
                .map(ep -> ep.getPermission().getPerNo())
                .collect(Collectors.toList()));
        return dto;
    }

    private Employee convertToEntity(EmployeeDTO dto) {
        Employee employee = modelMapper.map(dto, Employee.class);

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다. ID: " + dto.getDepartmentId()));
            employee.setDepartment(department);
        }

        if (dto.getRankId() != null) {
            Rank rank = rankRepository.findById(dto.getRankId())
                    .orElseThrow(() -> new EntityNotFoundException("직급을 찾을 수 없습니다. ID: " + dto.getRankId()));
            employee.setRank(rank);
        }

        return employee;
    }

    private void updateEmployeeFromDTO(Employee employee, EmployeeDTO employeeDTO) {
        if (employeeDTO.getFirstName() != null) {
            employee.setFirstName(employeeDTO.getFirstName());
        }
        if (employeeDTO.getLastName() != null) {
            employee.setLastName(employeeDTO.getLastName());
        }
        if (employeeDTO.getPhoneNum() != null) {
            employee.setPhoneNum(employeeDTO.getPhoneNum());
        }
        if (employeeDTO.getBirthDate() != null) {
            employee.setBirthDate(employeeDTO.getBirthDate());
        }
        if (employeeDTO.getAddr() != null) {
            employee.setAddr(employeeDTO.getAddr());
        }
        if (employeeDTO.getEmail() != null) {
            employee.setEmail(employeeDTO.getEmail());
        }
        if (employeeDTO.getLastDate() != null) {
            employee.setLastDate(employeeDTO.getLastDate());
        }

        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다. ID: " + employeeDTO.getDepartmentId()));
            employee.setDepartment(department);
        }

        if (employeeDTO.getRankId() != null) {
            Rank rank = rankRepository.findById(employeeDTO.getRankId())
                    .orElseThrow(() -> new EntityNotFoundException("직급을 찾을 수 없습니다. ID: " + employeeDTO.getRankId()));
            employee.setRank(rank);
        }
    }


    // 박성은 추가 코드
    // 로그인한 사원번호를 제외한 전체 사원 목록 조회
    public List<EmployeeDTO> getAllEmployeesExcept(Long loggedInEmpNo) {
        List<Employee> employees;

        if (loggedInEmpNo != null) {
            employees = employeeRepository.findAllByEmpNoNot(loggedInEmpNo);
        } else {
            employees = employeeRepository.findAll();
        }

        return employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}

