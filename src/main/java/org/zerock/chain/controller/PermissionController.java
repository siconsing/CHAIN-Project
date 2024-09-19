package org.zerock.chain.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.dto.PermissionDTO;
import org.zerock.chain.exception.EmployeeNotFoundException;
import org.zerock.chain.exception.PermissionNotFoundException;
import org.zerock.chain.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/employees/{id}/permissions")
    public ResponseEntity<List<PermissionDTO>> getPermissionsByEmployeeId(@PathVariable("id") Long id) {
        try {
            List<PermissionDTO> permissions = permissionService.getPermissionsByEmployeeId(id);
            return ResponseEntity.ok(permissions);
        } catch (EmployeeNotFoundException ex) {
            logger.error("Employee not found with ID: {}", id, ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PutMapping("/employees/{id}/permissions")
    public ResponseEntity<Void> updatePermissionsByEmployeeId(
            @PathVariable("id") Long id,
            @RequestBody List<Long> permissionIds) {

//        // 입력 데이터 검증: permissionIds가 null이거나 비어 있는지 확인
//        if (permissionIds == null || permissionIds.isEmpty()) {
//            logger.error("권한 ID 목록이 비어 있거나 유효하지 않습니다. 직원 ID: {}", id);
//            return ResponseEntity.badRequest().build();  // 400 Bad Request 반환
//        }

        try {

            // 권한 ID 목록이 null이거나 비어 있는 경우: 모든 권한 제거
            if (permissionIds == null || permissionIds.isEmpty()) {
                logger.info("권한 ID 목록이 비어 있습니다. 모든 권한을 제거합니다. 직원 ID: {}", id);
                permissionService.updateEmployeePermissions(id, List.of());  // 빈 리스트로 권한 제거
                return ResponseEntity.noContent().build();  // 204 No Content 반환
            }

            // 권한 업데이트 시도
            permissionService.updateEmployeePermissions(id, permissionIds);
            return ResponseEntity.noContent().build();  // 204 No Content 반환

        } catch (EmployeeNotFoundException ex) {
            // 직원이 존재하지 않는 경우 예외 처리
            logger.error("해당 ID의 직원을 찾을 수 없습니다. 직원 ID: {}", id, ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 404 Not Found 반환

        } catch (PermissionNotFoundException ex) {
            // 권한이 존재하지 않는 경우 예외 처리
            logger.error("해당 ID의 권한을 찾을 수 없습니다: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 404 Not Found 반환

        } catch (DataIntegrityViolationException ex) {
            // 중복 키 등 데이터 무결성 예외 처리
            logger.error("권한 업데이트 중 데이터 무결성 위반이 발생했습니다. 직원 ID: {}", id, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();  // 409 Conflict 반환

        } catch (Exception ex) {
            // 기타 예외 처리
            logger.error("권한 업데이트 중 예기치 못한 오류가 발생했습니다. 직원 ID: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 500 Internal Server Error 반환
        }
    }

}
