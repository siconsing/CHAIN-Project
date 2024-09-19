package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // 추가적인 부서 관련 쿼리 메서드를 정의할 수 있습니다.
}