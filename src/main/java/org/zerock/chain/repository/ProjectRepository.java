package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Project;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 특정 사원번호(empNo)를 가진 모든 프로젝트 조회
    List<Project> findByEmpNo(Long empNo);

    // 특정 사원번호(empNo)를 가진 임시보관된 모든 프로젝트 조회
    List<Project> findByEmpNoAndIsTemporary(Long empNo, boolean isTemporary);

    // 임시보관된 모든 프로젝트 조회
    List<Project> findByIsTemporary(boolean isTemporary);

}
