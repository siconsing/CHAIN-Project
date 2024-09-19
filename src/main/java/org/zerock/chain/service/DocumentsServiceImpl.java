package org.zerock.chain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.model.Employee;
import org.zerock.chain.repository.ApprovalRepository;
import org.zerock.chain.repository.EmployeeRepository;
import org.zerock.chain.dto.DocumentStatusCountDTO;
import org.zerock.chain.dto.DocumentsDTO;
import org.zerock.chain.model.Approval;
import org.zerock.chain.model.Documents;
import org.zerock.chain.repository.DocumentsRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class DocumentsServiceImpl implements DocumentsService<DocumentsDTO> {

    private final DocumentsRepository documentsRepository;
    private final EmployeeRepository employeeRepository;
    private final ApprovalRepository approvalRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService; // 파일 저장 서비스를 주입
    private final UserService userService; // 사용자 정보 서비스

    @Override
    public DocumentsDTO getDocumentById(int docNo) {
        Documents document = documentsRepository.findById(docNo).orElseThrow(() -> new RuntimeException("Document not found"));
        DocumentsDTO dto = modelMapper.map(document, DocumentsDTO.class);

        return dto;
    }

    @Override
    public List<DocumentsDTO> getSentDocuments(Long loggedInEmpNo) {
        // 보낸 문서 목록을 조회하여 DTO로 변환
        List<Documents> documents = documentsRepository.findSentDocuments(loggedInEmpNo);

        return documents.stream()
                .map(doc -> {
                    DocumentsDTO dto = modelMapper.map(doc, DocumentsDTO.class);

                    // senderName을 employees 테이블에서 조회하여 설정
                    String senderName = employeeRepository.findFullNameByEmpNo(doc.getLoggedInEmpNo());
                    dto.setSenderName(senderName);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentsDTO> getDraftDocuments(Long loggedInEmpNo) {
        // 임시 문서 목록을 조회하여 DTO로 변환
        List<Documents> documents = documentsRepository.findDraftDocuments(loggedInEmpNo);
        return documents.stream()
                .map(doc -> {
                    DocumentsDTO dto = modelMapper.map(doc, DocumentsDTO.class);

                    // senderName을 employees 테이블에서 조회하여 설정
                    String senderName = employeeRepository.findFullNameByEmpNo(doc.getLoggedInEmpNo());
                    dto.setSenderName(senderName);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public int saveDocument(DocumentsDTO documentsDTO) {
        try {
            // 파일이 있는 경우 파일 저장 처리
            String filePath = null;
            if (documentsDTO.getFile() != null && !documentsDTO.getFile().isEmpty()) {
                try {
                    filePath = fileService.saveFile(documentsDTO.getFile());  // 파일을 저장하고 경로를 반환
                    documentsDTO.setFilePath(filePath);  // DTO에 파일 경로 설정
                } catch (IOException e) {
                    throw new RuntimeException("파일 저장에 실패했습니다.", e);
                }
            }

            // 로그인한 사용자의 정보를 userService의 getLoggedInUserEmpNo메서드로 가져오기
            Long loggedInEmpNo = userService.getLoggedInUserEmpNo();

            // 로그인한 사용자의 정보를 Employees 테이블에서 조회
            Employee employee = employeeRepository.findById(loggedInEmpNo)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 작성자 이름, 부서명, 직급명 설정
            String senderName = employee.getLastName() + employee.getFirstName();
            String senderDmpName = employee.getDepartment().getDmpName();
            String senderRankName = employee.getRank().getRankName();

            // DocumentsDTO에 필요한 필드 설정
            documentsDTO.setLoggedInEmpNo(loggedInEmpNo);
            documentsDTO.setSenderName(senderName);

            log.info("senderName: {}, senderDmpName: {}, senderRankName: {}", senderName, senderDmpName, senderRankName);

            // Documents 엔티티를 먼저 생성하고 저장
            Documents documents = Documents.builder()
                    .reqDate(LocalDate.now())
                    .loggedInEmpNo(loggedInEmpNo)
                    .senderName(senderName)                  // 작성자를 저장
                    .senderDmpName(senderDmpName)            // 작성자 부서명
                    .senderRankName(senderRankName)          // 작성자 직급명
                    .docStatus(documentsDTO.getDocStatus())  // 요청된 상태를 사용
                    .category(documentsDTO.getCategory())    // 클라이언트가 보낸 카테고리 설정
                    .docTitle(documentsDTO.getDocTitle())    // 문서 제목을 설정
                    .docBody(documentsDTO.getDocBody())      // 내용 입력값을 저장
                    .approvalLine(documentsDTO.getApprovalLine())      // 결재자 정보를 저장
                    .timeStampHtml(documentsDTO.getTimeStampHtml())    // 타임스탬프를 저장
                    .approverNoHtml(documentsDTO.getApproverNoHtml())  // 결재자 순번을 저장
                    .filePath(filePath)                                // 저장된 파일 경로 설정
                    .approversJson(documentsDTO.getApproversJson())    // 결재자 정보(결재자 사원 번호, 결재 순번)을 저장
                    .referencesJson(documentsDTO.getReferencesJson())  // 참조자 정보 - 참조자 사원 번호
                    .build();

            Documents savedDocument = documentsRepository.save(documents);
            log.info("Saved document: {}", savedDocument);

            return savedDocument.getDocNo();
        } catch (Exception e) {
            // 예외 발생 시 에러 로그 출력
            log.error("Error occurred while saving document and form", e);
            // 서비스 계층에서 예외를 던지지 않고 적절한 기본값을 반환
            return -1;
        }
    }

    @Override
    public void updateDocument(DocumentsDTO documentsDTO) throws Exception {

        // 결재자 정보 삭제
        approvalRepository.deleteByDocumentsDocNo(documentsDTO.getDocNo());

        // 기존 문서를 데이터베이스에서 찾음
        Optional<Documents> optionalDocument = documentsRepository.findById(documentsDTO.getDocNo());
        if (optionalDocument.isEmpty()) {
            throw new Exception("Document not found");
        }

        Documents document = optionalDocument.get();

        // 파일이 있는 경우 파일 저장 처리
        if (documentsDTO.getFile() != null && !documentsDTO.getFile().isEmpty()) {
            String filePath = fileService.saveFile(documentsDTO.getFile());  // 파일을 저장하고 경로를 반환
            documentsDTO.setFilePath(filePath);  // DTO에 파일 경로 설정
            document.setFilePath(filePath);  // 엔티티의 파일 경로를 업데이트
        }

        // 문서의 다른 필드들 업데이트
        document.setDocTitle(documentsDTO.getDocTitle());
        document.setDocBody(documentsDTO.getDocBody());
        document.setApprovalLine(documentsDTO.getApprovalLine());
        document.setTimeStampHtml(documentsDTO.getTimeStampHtml());
        document.setApproverNoHtml(documentsDTO.getApproverNoHtml());
        document.setDocStatus(documentsDTO.getDocStatus());
        document.setCategory(documentsDTO.getCategory());
        document.setApproversJson(documentsDTO.getApproversJson());
        document.setReferencesJson(documentsDTO.getReferencesJson());

        // 문서 업데이트
        Documents savedDocument = documentsRepository.save(document);
        log.info("Updated document: {}", savedDocument);
    }

    @Override
    public void updateDocumentWithoutDeletingApprovals(DocumentsDTO documentsDTO) throws Exception {

        // 기존 문서를 데이터베이스에서 찾음
        Optional<Documents> optionalDocument = documentsRepository.findById(documentsDTO.getDocNo());
        if (optionalDocument.isEmpty()) {
            throw new Exception("Document not found");
        }

        Documents document = optionalDocument.get();

        // 파일이 있는 경우 파일 저장 처리
        if (documentsDTO.getFile() != null && !documentsDTO.getFile().isEmpty()) {
            String filePath = fileService.saveFile(documentsDTO.getFile());  // 파일을 저장하고 경로를 반환
            documentsDTO.setFilePath(filePath);  // DTO에 파일 경로 설정
            document.setFilePath(filePath);  // 엔티티의 파일 경로를 업데이트
        }

        // 문서의 다른 필드들 업데이트
        document.setDocTitle(documentsDTO.getDocTitle());
        document.setDocBody(documentsDTO.getDocBody());
        document.setApprovalLine(documentsDTO.getApprovalLine());
        document.setTimeStampHtml(documentsDTO.getTimeStampHtml());
        document.setApproverNoHtml(documentsDTO.getApproverNoHtml());
        document.setDocStatus(documentsDTO.getDocStatus());
        document.setCategory(documentsDTO.getCategory());
        document.setApproversJson(documentsDTO.getApproversJson());
        document.setReferencesJson(documentsDTO.getReferencesJson());

        // 문서 업데이트
        Documents savedDocument = documentsRepository.save(document);
        log.info("Updated document without deleting approvals: {}", savedDocument);
    }


    @Override
    public void deleteDocument(int docNo) throws Exception {
        // 문서를 데이터베이스에서 찾음
        Optional<Documents> optionalDocument = documentsRepository.findById(docNo);
        if (optionalDocument.isEmpty()) {
            throw new Exception("Document not found");
        }

        // 문서 삭제
        documentsRepository.deleteById(docNo);
    }

    // time Stamp documents테이블에 div태그로 저장된 컬럼 업데이트할 때 쓰는 메서드
    @Override
    public void updateTimeStampHtml(int docNo, String timeStampHtml) {
        Documents document = documentsRepository.findById(docNo)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setTimeStampHtml(timeStampHtml);  // time_stamp_html 컬럼 업데이트
        documentsRepository.save(document);
    }

    // 결재순서 가져오는 메서드
    @Override
    @Transactional(readOnly = true)
    public DocumentsDTO getDocumentWithApprovalOrder(int docNo, Long empNo) {
        // 문서 조회
        Documents document = documentsRepository.findById(docNo)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // DocumentsDTO로 변환
        DocumentsDTO dto = modelMapper.map(document, DocumentsDTO.class);

        // 로그인한 사용자의 결재 순서 가져오기
        Approval approval = approvalRepository.findByDocumentsDocNoAndEmployeeEmpNo(docNo, empNo);
        if (approval != null) {
            dto.setApprovalOrder(approval.getApprovalOrder());      // 결재 순번 추가
            dto.setApprovalStatus(approval.getApprovalStatus());    // 결재 진행 상태 추가

            // 해당 문서의 모든 결재자들의 반려 사유를 확인
            List<Approval> approvals = approvalRepository.findByDocumentsDocNo(docNo);
            for (Approval app : approvals) {
                if (app.getRejectionReason() != null && !app.getRejectionReason().isEmpty()) {
                    dto.setRejectionReason(app.getRejectionReason());  // 반려 사유가 있는 경우 설정
                    break; // 첫 번째 반려 사유를 찾으면 종료
                }
            }
        } else {
            dto.setApprovalOrder(-1);     // 결재선에 포함되지 않은 경우
            dto.setApprovalStatus("N/A"); // 결재선에 포함되지 않은 경우 기본값 설정
        }

        return dto;
    }

    @Override
    public DocumentStatusCountDTO getDocumentStatusCountsForUser(Long empNo) {
        int requestsCount = documentsRepository.countByDocStatusAndEmpNo("요청", empNo);
        int inProgressCount = documentsRepository.countByDocStatusAndEmpNo("진행 중", empNo);
        int rejectedCount = documentsRepository.countByDocStatusAndEmpNo("반려", empNo);
        int completedCount = documentsRepository.countByDocStatusAndEmpNo("완료", empNo);

        log.info("requestsCount: " + requestsCount);
        log.info("inProgressCount: " + inProgressCount);
        log.info("rejectedCount: " + rejectedCount);
        log.info("completedCount: " + completedCount);

        // 빌더 패턴 대신 생성자를 사용하여 객체 생성
        return new DocumentStatusCountDTO(requestsCount, inProgressCount, rejectedCount, completedCount);
    }

    @Override  // 상태별 문서목록을 조회
    public List<DocumentsDTO> getDocumentsByStatus(Long loggedInEmpNo, String docStatus) {
        // 상태별 문서 목록을 조회하여 DTO로 변환
        List<Documents> documents = documentsRepository.findDocumentsByStatusAndEmpNo(loggedInEmpNo, docStatus);

        return documents.stream()
                .map(doc -> {
                    DocumentsDTO dto = modelMapper.map(doc, DocumentsDTO.class);

                    // senderName을 employees 테이블에서 조회하여 설정
                    String senderName = employeeRepository.findFullNameByEmpNo(doc.getLoggedInEmpNo());
                    dto.setSenderName(senderName);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override  // 받은문서함의 대기중인 문서 필터링
    public List<DocumentsDTO> getPendingDocumentsForUser(Long empNo) {
        List<Documents> documents = documentsRepository.findPendingDocumentsByEmpNo(empNo);
        return documents.stream()
                .map(doc -> modelMapper.map(doc, DocumentsDTO.class))
                .collect(Collectors.toList());
    }

    @Override  // 받은문서함의 승인한 문서 필터링
    public List<DocumentsDTO> getApprovedDocumentsForUser(Long empNo) {
        List<Documents> documents = documentsRepository.findApprovedDocumentsByEmpNo(empNo);
        return documents.stream()
                .map(doc -> modelMapper.map(doc, DocumentsDTO.class))
                .collect(Collectors.toList());
    }

    @Override  // 받은문서함의 반려된 문서 필터링(이 때 다른 결재자가 반려된 문서여도 필터링)
    public List<DocumentsDTO> getRejectedDocumentsForUser(Long empNo) {
        List<Documents> documents = documentsRepository.findRejectedDocumentsIncludingOthers(empNo);
        return documents.stream()
                .map(doc -> modelMapper.map(doc, DocumentsDTO.class))
                .collect(Collectors.toList());
    }

    @Override  // 받은문서함의 참조된 문서 필터링
    public List<DocumentsDTO> getReferencesDocumentsForUser(Long empNo) {
        List<Documents> documents = documentsRepository.findReferencedDocumentsByEmpNo(empNo);
        return documents.stream()
                .map(doc -> modelMapper.map(doc, DocumentsDTO.class))
                .collect(Collectors.toList());
    }

    @Override  // 해당 문서의 반려사유를 들고오는 메서드
    public String getRejectionReason(int docNo) {
        return approvalRepository.findRejectionReasonByDocNo(docNo);
    }
}
