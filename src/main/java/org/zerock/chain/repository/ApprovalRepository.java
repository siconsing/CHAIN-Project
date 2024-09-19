package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Approval;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // 로그인한 사용자가 결재자 또는 참조자인 문서를 docNo 기준으로 최신순 조회
    @Query("SELECT a FROM Approval a " +
            "LEFT JOIN a.employee e " +   // 결재자 조인
            "LEFT JOIN a.refEmployee re " + // 참조자 조인
            "WHERE e.empNo = :empNo OR re.empNo = :empNo " +
            "ORDER BY a.documents.docNo DESC")
    List<Approval> findByEmployeeOrRefEmployee(@Param("empNo") Long empNo);

    // 결재자가 어떤 순서에 있든 자신에게 지정된 모든 결재 문서를 조회
    Approval findByDocumentsDocNoAndEmployeeEmpNo(int docNo, Long empNo);

    // approvals에서 특정 문서 번호와 결재 순서로 결재 내역을 조회
    Approval findByDocumentsDocNoAndApprovalOrder(int docNo, int approvalOrder);

    // approvals에서 특정 문서 번호의 모든 결재 내역을 조회 (결재선 조회 시 사용 가능)
    List<Approval> findByDocumentsDocNo(int docNo);

    // 로그인한 결재자별로 대기 상태인 문서 개수 조회
    @Query("SELECT COUNT(a) FROM Approval a WHERE a.approvalStatus = '대기' AND a.employee.empNo = :empNo")
    int countPendingApprovalsByEmpNo(@Param("empNo") Long empNo);

    // 로그인한 결재자별로 승인 상태인 문서 개수 조회
    @Query("SELECT COUNT(a) FROM Approval a WHERE a.approvalStatus = '승인' AND a.employee.empNo = :empNo")
    int countApprovedApprovalsByEmpNo(@Param("empNo") Long empNo);

    // 로그인한 결재자의 반려된 문서가 하나라도 있다면 개수로 추가
    @Query("SELECT COUNT(DISTINCT a1.documents.docNo) " +
            "FROM Approval a1 " +
            "WHERE a1.employee.empNo = :empNo " +
            "AND EXISTS (" +
            "    SELECT 1 FROM Approval a2 " +
            "    WHERE a2.documents.docNo = a1.documents.docNo AND a2.approvalStatus = '반려'" +
            ")")
    int countRejectedDocumentsForApprover(@Param("empNo") Long empNo);

    // 로그인한 사용자가 참조자인 문서의 수를 조회하는 쿼리
    @Query("SELECT COUNT(a) FROM Approval a " +
            "WHERE a.refEmployee.empNo = :empNo AND a.approvalStatus = '참조'")
    int countReferencedDocumentsForUser(@Param("empNo") Long empNo);

    // 로그인한 사원의 총 받은 문서 수를 조회하는 쿼리 메서드
    @Query("SELECT COUNT(a) FROM Approval a " +
            "LEFT JOIN a.employee e " +   // 결재자 조인
            "LEFT JOIN a.refEmployee re " + // 참조자 조인
            "WHERE e.empNo = :empNo OR re.empNo = :empNo")
    int countDocumentsByEmployeeOrRefEmployee(@Param("empNo") Long empNo);

    // 특정 문서 번호에 해당하는 결재자 정보 삭제
    void deleteByDocumentsDocNo(int docNo);

    // 특정 문서 번호에 관련된 모든 결재자 사원의 번호를 가져옵니다.
    @Query("SELECT DISTINCT a.employee.empNo FROM Approval a WHERE a.documents.docNo = :docNo")
    List<Long> findEmpNosByDocNo(@Param("docNo") int docNo);

    // 특정 문서 번호에 관련된 모든 참조자 사원의 번호를 가져옵니다.
    @Query("SELECT DISTINCT a.refEmployee.empNo FROM Approval a WHERE a.documents.docNo = :docNo")
    List<Long> findRefEmpNosByDocNo(@Param("docNo") int docNo);

    // 반려 사유를 가져오는 쿼리 메서드
    @Query("SELECT a.rejectionReason FROM Approval a WHERE a.documents.docNo = :docNo AND a.approvalStatus = '반려'")
    String findRejectionReasonByDocNo(@Param("docNo") int docNo);
}