package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.EmployeeLeave;

@Repository // 영민 추가
public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {
    EmployeeLeave findByEmpNo(Long empNo);
}

