package org.zerock.chain.repository;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Employee;

import java.util.List;
import java.util.Optional;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e " +
            "JOIN FETCH e.department d " +
            "JOIN FETCH e.rank r " +
            "WHERE (:name IS NULL OR e.firstName LIKE %:name% OR e.lastName LIKE %:name%) " +
            "AND (:departmentName IS NULL OR d.dmpName = :departmentName) " +
            "AND (:rankName IS NULL OR r.rankName = :rankName)")
    List<Employee> findEmployeesByCriteria(
            @Param("name") String name,
            @Param("departmentName") String departmentName,
            @Param("rankName") String rankName);

    @EntityGraph(attributePaths = {"department", "rank"})
    List<Employee> findByDepartmentDmpNo(Long departmentId);

    @EntityGraph(attributePaths = {"department", "rank", "employeePermissions.permission"})
    Optional<Employee> findById(Long id);

    // 박성은 추가 코드
    List<Employee> findAllByEmpNoNot(Long empNo);

    // 부서,직급,연차 정보 들고오는 메서드(영민 추가)
    @EntityGraph(attributePaths = {"department", "rank", "employeeLeave"})
    List<Employee> findAll();

    @Query("SELECT CONCAT(e.lastName, e.firstName) FROM Employee e WHERE e.empNo = :empNo")
    String findFullNameByEmpNo(@Param("empNo") Long empNo);
}
