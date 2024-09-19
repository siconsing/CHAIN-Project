package org.zerock.chain.service;

import org.zerock.chain.dto.DocumentStatusCountDTO;
import org.zerock.chain.dto.DocumentsDTO;

import java.util.List;

public interface DocumentsService <T extends DocumentsDTO> {

    // 여기에 서비스Impl에 필요한 메서드를 정의

    // 로그인한 사용자가 자신의 보낸 문서함, 받은 문서함, 임시 문서함에서 자신과 관련된 문서만 조회할 수 있는 메서드
    List<T> getSentDocuments(Long loggedInEmpNo);  // 보낸 문서 목록 조회
    List<T> getDraftDocuments(Long loggedInEmpNo);  // 임시 문서 목록 조회

    // 사용자가 문서에 입력한 모든 정보를 반환하는 메서드
    int saveDocument(DocumentsDTO documentsDTO);
    // 사용자가 임시저장한 문서에서 정보를 업데이트하는 메서드
    void updateDocument(DocumentsDTO documentsDTO) throws Exception;
    // 사용자가 임시저장한 문서 삭제하는 메서드
    void deleteDocument(int docNo) throws Exception;
    // 결재자가 결재 승인하면 TimeStamp 업데이트 된걸 저장하는 메서드
    void updateTimeStampHtml(int docNo, String timeStampHtml);
    // 문서의 반려 사유를 가져오는 메서드
    String getRejectionReason(int docNo);
    // 임시문서에서 결재요청하면 임시저장과 달리 결재자 정보 삭제하지않고 그대로 저장되는 메서드
    void updateDocumentWithoutDeletingApprovals(DocumentsDTO documentsDTO) throws Exception;

    // 문서 번호로 문서 조회 (각 read, draftRead 등 read.html 들어갈 때 필요한 메서드)
    T getDocumentById(int docNo);
    // 문서 조회 및 결재 순서 포함 메서드
    DocumentsDTO getDocumentWithApprovalOrder(int docNo, Long empNo);
    // 보낸 문서 상태별 문서 건수 조회
    DocumentStatusCountDTO getDocumentStatusCountsForUser(Long empNo);
    // 보낸 문서 상태별 문서 목록 조회
    List<DocumentsDTO> getDocumentsByStatus(Long loggedInEmpNo, String docStatus);
    // 대기중인 문서 필터링
    List<DocumentsDTO> getPendingDocumentsForUser(Long empNo);
    // 승인한 문서 필터링
    List<DocumentsDTO> getApprovedDocumentsForUser(Long empNo);
    // 반려된 문서 필터링
    List<DocumentsDTO> getRejectedDocumentsForUser(Long empNo);
    // 참조된 문서 필터링
    List<DocumentsDTO> getReferencesDocumentsForUser(Long empNo);
}
