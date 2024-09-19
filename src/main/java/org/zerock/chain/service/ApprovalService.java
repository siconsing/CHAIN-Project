package org.zerock.chain.parkyeongmin.service;


import org.zerock.chain.dto.DocumentsDTO;

public interface ApprovalService {
    // 결재 요청을 처리하는 메서드
    void requestApproval(DocumentsDTO documentsDTO);

    // 결재를 승인하는 메서드
    void approveDocument(int docNo, Long empNo);

    // 결재를 반려하는 메서드
    void rejectDocument(int docNo, Long empNo, String rejectionReason);

    // 문서를 다음 결재자에게 할당하는 메서드
    void moveToNextApprover(int docNo, int nextOrder);

    // 문서를 최종 승인 상태로 변경하는 메서드
    void finalizeDocument(int docNo);

    // 첫 번째 결재자가 승인했는지 여부를 판단하는 메서드
    boolean isFirstApprovalApproved(int docNo);

    // 현재 결재자의 결재 순서로 승인,반려 버튼을 숨기기 위해 쓰는 메서드
    boolean isCurrentApprover(int docNo, Long empNo);

    // 해당 문서의 결재자에 현재 사용자가 포함되어 있는지 확인
    boolean isDocumentApprover(int docNo, Long empNo);

    // 로그인한 사용자(결재자)의 받은 문서함에 대기상태 문서 수 조회
    int countPendingApprovals(Long empNo);

    // 로그인한 사용자(결재자)의 받은 문서함에 승인상태 문서 수 조회
    int countApprovedApprovals(Long empNo);

    // 로그인한 사용자(결재자)의 받은 문서함에 반려상태 문서 수 조회
    int countRejectedDocumentsForApprover(Long empNo);

    // 로그인한 사용자(참조자)의 받은 문서함에 반려상태 문서 수 조회
    int countReferencesDocumentsForUser(Long empNo);

    // 로그인한 사원의 총 받은 문서 수 조회
    int countTotalDocumentsForApprover(Long empNo);
}