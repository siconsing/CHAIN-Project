package org.zerock.chain.imjongha.repository;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.imjongha.model.Employee;

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
}
