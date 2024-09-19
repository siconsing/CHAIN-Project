package org.zerock.chain.service;

import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.dto.PermissionDTO;
import org.zerock.chain.exception.EmployeeNotFoundException;
import org.zerock.chain.model.Employee;
import org.zerock.chain.model.EmployeePermission;
import org.zerock.chain.model.Permission;
import org.zerock.chain.repository.EmployeePermissionRepository;
import org.zerock.chain.repository.EmployeeRepository;
import org.zerock.chain.repository.PermissionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final EmployeeRepository employeeRepository;
    private final PermissionRepository permissionRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;
    private final EmployeePermissionRepository employeePermissionRepository;


    public PermissionServiceImpl(EmployeeRepository employeeRepository,
                                 PermissionRepository permissionRepository,
                                 ModelMapper modelMapper,
                                 EntityManager entityManager, EmployeePermissionRepository employeePermissionRepository) {
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
        this.employeePermissionRepository = employeePermissionRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<PermissionDTO> getPermissionsByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("사원을 찾을 수 없습니다. ID: " + employeeId));
        return employee.getEmployeePermissions().stream()
                .map(ep -> modelMapper.map(ep.getPermission(), PermissionDTO.class))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = false)
    @Override
    public void updateEmployeePermissions(Long empNo, List<Long> permissionIds) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EmployeeNotFoundException("사원을 찾을 수 없습니다."));

        // 현재 권한 목록을 가져옴
        List<Long> existingPermissionIds = employee.getEmployeePermissions().stream()
                .map(ep -> ep.getPermission().getPerNo())
                .collect(Collectors.toList());

        // 추가할 권한 결정
        List<Long> permissionsToAdd = permissionIds.stream()
                .filter(permissionId -> !existingPermissionIds.contains(permissionId))
                .collect(Collectors.toList());

        // 제거할 권한 결정
        List<EmployeePermission> permissionsToRemove = employee.getEmployeePermissions().stream()
                .filter(ep -> !permissionIds.contains(ep.getPermission().getPerNo()))
                .collect(Collectors.toList());

        // 기존 권한 삭제
        if (!permissionsToRemove.isEmpty()) {
            employeePermissionRepository.deleteAll(permissionsToRemove);
            entityManager.flush();  // 변경 사항을 즉시 데이터베이스에 반영
            employee.getEmployeePermissions().removeAll(permissionsToRemove);  // 메모리에서 제거
        }

        // 새로운 권한 추가
        List<Permission> permissions = permissionRepository.findAllById(permissionsToAdd);
        permissions.forEach(permission -> {
            EmployeePermission employeePermission = new EmployeePermission(employee, permission);
            employee.getEmployeePermissions().add(employeePermission);  // 메모리에 추가
            employeePermissionRepository.save(employeePermission);  // 데이터베이스에 저장
        });

        // 최종적으로 employee 엔티티 저장
        employeeRepository.save(employee);
    }



    @Transactional(readOnly = true)
    @Override
    public List<PermissionDTO> getEmployeePermissions(Long empNo) {
        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new EmployeeNotFoundException("사원을 찾을 수 없습니다. ID: " + empNo));
        return employee.getEmployeePermissions().stream()
                .map(ep -> modelMapper.map(ep.getPermission(), PermissionDTO.class))
                .collect(Collectors.toList());
    }

    private PermissionDTO convertToDTO(Permission permission) {
        return modelMapper.map(permission, PermissionDTO.class);
    }

    private Permission convertToEntity(PermissionDTO dto) {
        return modelMapper.map(dto, Permission.class);
    }
}
