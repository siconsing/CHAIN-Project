package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Report;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 임시보관된 보고서 목록 조회
    List<Report> findByIsTemporary(boolean isTemporary);
}