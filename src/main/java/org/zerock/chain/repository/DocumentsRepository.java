package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Documents;

import java.util.List;

@Repository
public interface DocumentsRepository extends JpaRepository<Documents, Integer> {

    // 보낸 문서함 목록 조회
    @Query("SELECT d FROM Documents d WHERE d.loggedInEmpNo = :loggedInEmpNo AND d.docStatus != '임시저장' ORDER BY d.docNo DESC")
    List<Documents> findSentDocuments(@Param("loggedInEmpNo") Long loggedInEmpNo);

    // 임시문서함 목록 조회
    @Query("SELECT d FROM Documents d WHERE d.docStatus = '임시저장' AND d.loggedInEmpNo = :loggedInEmpNo ORDER BY d.docNo DESC")
    List<Documents> findDraftDocuments(@Param("loggedInEmpNo") Long loggedInEmpNo);

    // 보낸문서함의 결재 진행 상태에 따른 각 문서 개수 조회
    @Query("SELECT COUNT(d) FROM Documents d WHERE d.docStatus = :docStatus AND d.loggedInEmpNo = :empNo")
    int countByDocStatusAndEmpNo(@Param("docStatus") String docStatus, @Param("empNo") Long empNo);

    @Query("SELECT d FROM Documents d WHERE d.loggedInEmpNo = :empNo AND d.docStatus = :docStatus ORDER BY d.docNo DESC")
    List<Documents> findDocumentsByStatusAndEmpNo(@Param("empNo") Long empNo, @Param("docStatus") String docStatus);

    // 대기중인 문서 필터링
    @Query("SELECT DISTINCT d FROM Documents d " +
            "JOIN Approval a ON d.docNo = a.documents.docNo " +
            "WHERE a.employee.empNo = :empNo " +
            "AND a.approvalStatus = '대기' " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM Approval a2 " +
            "    WHERE a2.documents = d AND a2.approvalStatus = '반려'" +
            ") ORDER BY d.docNo DESC")
    List<Documents> findPendingDocumentsByEmpNo(@Param("empNo") Long empNo);

    // 승인한 문서 필터링
    @Query("SELECT DISTINCT d FROM Documents d " +
            "JOIN Approval a ON d.docNo = a.documents.docNo " +
            "WHERE a.employee.empNo = :empNo " +
            "AND a.approvalStatus = '승인' " +
            "ORDER BY d.docNo DESC")
    List<Documents> findApprovedDocumentsByEmpNo(@Param("empNo") Long empNo);

    // 반려된 문서 필터링
    @Query("SELECT DISTINCT d FROM Documents d " +
            "JOIN Approval a1 ON d.docNo = a1.documents.docNo " +
            "WHERE a1.employee.empNo = :empNo " +
            "AND EXISTS (" +
            "    SELECT 1 FROM Approval a2 " +
            "    WHERE a2.documents = d AND a2.approvalStatus = '반려'" +
            ") " +
            "ORDER BY d.docNo DESC")
    List<Documents> findRejectedDocumentsIncludingOthers(@Param("empNo") Long empNo);

    // 참조자로 지정된 문서만 필터링하는 쿼리
    @Query("SELECT d FROM Documents d " +
            "JOIN Approval a ON d.docNo = a.documents.docNo " +
            "WHERE a.refEmployee.empNo = :empNo AND a.approvalStatus = '참조' " +
            "ORDER BY d.docNo DESC")
    List<Documents> findReferencedDocumentsByEmpNo(@Param("empNo") Long empNo);
}
