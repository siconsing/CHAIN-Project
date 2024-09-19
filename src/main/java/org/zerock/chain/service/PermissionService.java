package org.zerock.chain.service;

import org.zerock.chain.dto.PermissionDTO;

import java.util.List;

public interface PermissionService {
    /**
     * 모든 권한을 조회합니다.
     *
     * @return 권한 목록
     */
    List<PermissionDTO> getAllPermissions();

    /**
     * 특정 사원의 권한을 조회합니다.
     *
     * @param employeeId 사원 ID
     * @return 사원의 권한 목록
     */
    List<PermissionDTO> getPermissionsByEmployeeId(Long employeeId);

    /**
     * 특정 사원의 권한을 업데이트합니다.
     *
     * @param empNo        사원 ID
     * @param permissionIds 권한 ID 목록
     */
    void updateEmployeePermissions(Long empNo, List<Long> permissionIds);

    /**
     * 특정 사원의 권한을 조회합니다.
     *
     * @param empNo 사원 ID
     * @return 권한 목록
     */
    List<PermissionDTO> getEmployeePermissions(Long empNo);
}
