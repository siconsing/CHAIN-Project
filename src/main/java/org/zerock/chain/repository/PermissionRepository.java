package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.chain.model.Permission;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    // 기존의 메소드 대신 JPQL 쿼리를 직접 작성하여 해결
    @Query("SELECT p FROM Permission p JOIN p.employeePermissions ep WHERE ep.employee.empNo = :employeeId")
    List<Permission> findPermissionsByEmployeeId(@Param("employeeId") Long empNo);
}
