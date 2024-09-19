package org.zerock.chain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.DocumentsDTO;
import org.zerock.chain.model.Approval;
import org.zerock.chain.model.Documents;
import org.zerock.chain.model.Employee;
import org.zerock.chain.repository.ApprovalRepository;
import org.zerock.chain.repository.DocumentsRepository;
import org.zerock.chain.service.NotificationService;
import org.zerock.chain.repository.EmployeeRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class ApprovalServiceImpl implements org.zerock.chain.parkyeongmin.service.ApprovalService {
    private final ApprovalRepository approvalRepository;
    private final DocumentsRepository documentsRepository;
    private final EmployeeRepository employeesRepository;
    private final NotificationService notificationService;  // 추가된 부분

    // approvals테이블에 사용자가 정한 정보가 저장되는 메서드
    @Override
    public void requestApproval(DocumentsDTO documentsDTO) {
        // approverJson을 파싱하여 List<Map<String, Object>>로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> approvers;  // 결재자 번호와 결재 순서를 저장할 리스트
        List<Map<String, Object>> references;  // 참조자 번호를 저장할 리스트

        try {
            approvers = objectMapper.readValue(documentsDTO.getApproversJson(), new TypeReference<>() {});
            log.info("Parsed approvers: {}", approvers);

            // referencesJson이 null이거나 빈 문자열인 경우에도 안전하게 처리
            if (documentsDTO.getReferencesJson() != null && !documentsDTO.getReferencesJson().isEmpty()) {
                references = objectMapper.readValue(documentsDTO.getReferencesJson(), new TypeReference<>() {});
                log.info("Parsed references: {}", references);
            } else {
                references = Collections.emptyList();
                log.info("No references provided.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing approverJson", e);
        }

        log.info("SenderName1:{}", documentsDTO.getSenderName());

        // 문서와 사원 객체를 조회하여 설정
        Documents document = documentsRepository.findById(documentsDTO.getDocNo())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // 결재선의 결재자들을 저장 (DocStatus는 변경하지 않음)
        for (Map<String, Object> approver : approvers) {

            // 문자열로 들어온 값을 Long과 Integer로 변환
            Long empNo = Long.valueOf(approver.get("empNo").toString());
            Integer approvalOrder = Integer.valueOf(approver.get("approvalOrder").toString());

            log.info("Processing approval for empNo: {}, approvalOrder: {}", empNo, approvalOrder);

            Employee employee = employeesRepository.findById(empNo)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            log.info("Found employee: {}", employee);

            // Approval 객체 생성 및 설정
            Approval approval = new Approval();
            approval.setDocuments(document);    // 문서 번호 설정
            approval.setEmployee(employee);     // 결재자 사원번호 설정
            approval.setApprovalOrder(approvalOrder);
            approval.setApprovalStatus("대기");  // 초기 상태는 대기

            approvalRepository.save(approval);
            log.info("Saved approval: {}", approval);
        }

        // 참조자가 여러 명일 경우, 각각에 대해 별도의 Approval 객체 생성 및 저장
        if (!references.isEmpty()) {
            for (Map<String, Object> reference : references) {

                Long refEmpNo = Long.valueOf(reference.get("refEmpNo").toString());
                Employee refEmployee = employeesRepository.findById(refEmpNo)
                        .orElseThrow(() -> new RuntimeException("Referenced employee not found"));
                Approval refApproval = new Approval();
                refApproval.setDocuments(document);    // 문서 번호 설정
                refApproval.setRefEmployee(refEmployee);  // 참조자 설정
                refApproval.setApprovalOrder(null);  // 참조자는 결재 순서가 없으므로 null로 설정
                refApproval.setApprovalStatus("참조");  // 참조자 상태 설정
                approvalRepository.save(refApproval);
                log.info("Saved reference approval: {}", refApproval);
            }
        }

        // 결재 차례인 사람에게 알림 생성
        notificationService.createApprovalNotification(documentsDTO.getDocNo(), documentsDTO.getDocTitle(), documentsDTO.getSenderName(), "대기 중", documentsDTO.getWithdraw());

        // 첫 번째 결재자에게 문서 할당
        Map<String, Object> firstApprover = approvers.get(0);
        // String 타입을 Long으로 바꾸기 위한 코드
        String empNoStr = (String) firstApprover.get("empNo");
        Long firstEmpNo = Long.parseLong(empNoStr);
        assignDocumentToApprover(documentsDTO.getDocNo(), firstEmpNo);
        log.info("Assigned document {} to first approver with empNo: {}", documentsDTO.getDocNo(), firstEmpNo);
    }

    @Override  // 결재자가 승인하면 그 승인이 작동되는 메서드
    public void approveDocument(int docNo, Long empNo) {
        // 문서 조회
        Documents document = documentsRepository.findById(docNo)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Approval approval = approvalRepository.findByDocumentsDocNoAndEmployeeEmpNo(docNo, empNo);

        if (approval != null) {
            approval.setApprovalDate(LocalDateTime.now());
            approval.setApprovalStatus("승인");  // 승인 상태로 변경
            approvalRepository.save(approval);
        }

        // 다음 결재자를 찾기 전에 현재 결재가 최종 결재인지 확인
        List<Approval> approvals = approvalRepository.findByDocumentsDocNo(docNo);
        boolean allApproved = approvals.stream()
                .allMatch(a -> "승인".equals(a.getApprovalStatus()));

        if (allApproved) {
            // 모든 결재가 승인되었으므로 문서를 완료로 설정
            finalizeDocument(docNo);
        } else {
            // 최종 결재가 아니므로 다음 결재자로 이동
            moveToNextApprover(docNo, approval.getApprovalOrder() + 1);

            // 다음 결재자에게 알림 전송
            Approval nextApproval = approvalRepository.findByDocumentsDocNoAndApprovalOrder(docNo, approval.getApprovalOrder() + 1);
            if (nextApproval != null) {
                notificationService.createApprovalNotification(docNo, document.getDocTitle(), document.getSenderName(), "진행 중", document.getWithdraw());
            }
        }
    }

    @Override
    public void rejectDocument(int docNo, Long empNo, String rejectionReason) {
        Approval approval = approvalRepository.findByDocumentsDocNoAndEmployeeEmpNo(docNo, empNo);

        if (approval != null) {
            approval.setApprovalStatus("반려");  // 반려 상태로 변경
            approval.setRejectionReason(rejectionReason);
            approvalRepository.save(approval);

            // 문서의 상태도 반려로 변경
            Documents document = documentsRepository.findById(docNo)
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            document.setDocStatus("반려");
            documentsRepository.save(document);

            // 결재 모든 관련자들에게 반려 알림 생성
            notificationService.createApprovalNotification(docNo, document.getDocTitle(), document.getSenderName(), "반려", document.getWithdraw());
        }
    }

    @Override       // 다음 결재자가 있다면 해야될 기능 처리
    public void moveToNextApprover(int docNo, int nextOrder) {
        Approval nextApproval = approvalRepository.findByDocumentsDocNoAndApprovalOrder(docNo, nextOrder);

        if (nextApproval != null) {
            // 다음 결재자가 있으면, 문서를 다음 결재자에게 할당하고, DocStatus를 "진행중"으로 변경
            assignDocumentToApprover(nextApproval.getDocuments().getDocNo(), nextApproval.getEmployee().getEmpNo());

            Documents document = documentsRepository.findById(docNo)
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            document.setDocStatus("진행 중");
            documentsRepository.save(document);
        } else {
            // 다음 결재자가 없으면, 문서를 최종 완료 상태로 변경
            finalizeDocument(docNo);
        }
    }

    @Override // 마지막 결재자에게 문서가 넘어가면 완료 처리
    public void finalizeDocument(int docNo) {
        Documents document = documentsRepository.findById(docNo)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setDocStatus("완료");
        documentsRepository.save(document);

        // 결재 모든 관련자들에게 완료 알림 생성
        notificationService.createApprovalNotification(docNo, document.getDocTitle(), document.getSenderName(), "완료", document.getWithdraw());

        System.out.println("Document No: " + docNo + " has been finalized.");
    }

    // 문서를 첫 번째 결재자에게 할당하는 메서드
    private void assignDocumentToApprover(int docNo, Long empNo) {
        // 문서를 조회
        Documents document = documentsRepository.findById(docNo)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // 첫 번째 결재자를 조회 (approvalOrder가 1인 결재자)
        Approval firstApproval = approvalRepository.findByDocumentsDocNoAndApprovalOrder(docNo, 1);

        if (firstApproval == null) {
            throw new RuntimeException("Approval not found for the given document and approver.");
        }

        // 문서를 첫 번째 결재자에게 할당했다고 로그 출력
        System.out.println("Document No: " + docNo + " has been assigned to the first approver with empNo: " + empNo);
    }

    @Override
    public boolean isFirstApprovalApproved(int docNo) {
        // 첫 번째 결재자의 상태를 조회
        Approval firstApproval = approvalRepository.findByDocumentsDocNoAndApprovalOrder(docNo, 1);

        if (firstApproval != null) {
            String approvalStatus = firstApproval.getApprovalStatus();
            log.info("First approval status for docNo {}: {}", docNo, approvalStatus); // 로그 추가

            // 첫 번째 결재자가 승인한 경우 true 반환
            return "승인".equals(approvalStatus);
        }

        log.info("No first approval found for docNo {}", docNo); // 로그 추가
        // 결재 정보가 없거나 승인이 아닌 경우 false 반환
        return false;
    }

    @Override
    public boolean isCurrentApprover(int docNo, Long empNo) {
        // 현재 사용자의 Approval 정보 조회
        Approval currentApproval = approvalRepository.findByDocumentsDocNoAndEmployeeEmpNo(docNo, empNo);

        if (currentApproval != null) {
            int currentOrder = currentApproval.getApprovalOrder();

            // 이전 결재자의 승인 여부 확인
            if (currentOrder > 1) {
                Approval previousApproval = approvalRepository.findByDocumentsDocNoAndApprovalOrder(docNo, currentOrder - 1);

                // 이전 결재자가 승인 또는 반려했을 때만 현재 결재자가 승인/반려 버튼을 볼 수 있음
                if (previousApproval != null && ("승인".equals(previousApproval.getApprovalStatus()))) {
                    return "대기".equals(currentApproval.getApprovalStatus());
                } else {
                    return false;
                }
            } else {
                // 첫 번째 결재자라면 이전 결재자가 없으므로 바로 승인/반려 가능
                return "대기".equals(currentApproval.getApprovalStatus());
            }
        }
        return false;
    }

    @Override
    public boolean isDocumentApprover(int docNo, Long empNo) {
        // 해당 문서의 결재자 목록에서 현재 사용자가 포함되어 있는지 확인
        Approval approval = approvalRepository.findByDocumentsDocNoAndEmployeeEmpNo(docNo, empNo);
        return approval != null;
    }

    @Override  // 결재자의 받은 문서함에 대기상태 문서 수 조회
    public int countPendingApprovals(Long empNo) {
        return approvalRepository.countPendingApprovalsByEmpNo(empNo);
    }

    @Override  // 결재자의 받은 문서함에 승인상태 문서 수 조회
    public int countApprovedApprovals(Long empNo) {
        return approvalRepository.countApprovedApprovalsByEmpNo(empNo);
    }

    @Override  // 결재자의 받은 문서함에 반려상태 문서 수 조회
    public int countRejectedDocumentsForApprover(Long empNo) {
        return approvalRepository.countRejectedDocumentsForApprover(empNo);
    }

    @Override  // 로그인한 사용자(참조자)의 받은 문서함에 참조된 문서 수 조회
    public int countReferencesDocumentsForUser(Long empNo) {
        return approvalRepository.countReferencedDocumentsForUser(empNo);
    }

    @Override  // 로그인한 사용자의 총 받은 문서 수 조회
    public int countTotalDocumentsForApprover(Long empNo) {
        return approvalRepository.countDocumentsByEmployeeOrRefEmployee(empNo);
    }
}