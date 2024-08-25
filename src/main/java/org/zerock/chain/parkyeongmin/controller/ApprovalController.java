package org.zerock.chain.parkyeongmin.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.parkyeongmin.dto.*;
import org.zerock.chain.parkyeongmin.model.Approval;
import org.zerock.chain.parkyeongmin.model.Documents;
import org.zerock.chain.parkyeongmin.repository.ApprovalRepository;
import org.zerock.chain.parkyeongmin.repository.EmployeesRepository;
import org.zerock.chain.parkyeongmin.service.ApprovalService;
import org.zerock.chain.parkyeongmin.service.DocumentsService;
import org.zerock.chain.parkyeongmin.service.FormService;
import org.zerock.chain.parkyeongmin.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/approval")
@Log4j2
public class ApprovalController {

    private final DocumentsService documentsService;
    private final FormService formService;  // FormService 주입
    private final UserService userService;
    private final ApprovalService approvalService;

    @Autowired
    private ApprovalRepository approvalRepository;
    @Autowired
    private EmployeesRepository employeesRepository;

    @Autowired
    public ApprovalController(DocumentsService documentsService,
                              FormService formService,
                              UserService userService,
                              ApprovalService approvalService) {
        this.documentsService = documentsService;
        this.formService = formService;
        this.userService = userService;
        this.approvalService = approvalService;
    }

    @GetMapping("/main")  // 보낸 문서함 페이지로 이동
    public String approvalMain(Model model) {
        Long loggedInEmpNo = 1L;
        List<SentDocumentsDTO> sentDocuments = documentsService.getSentDocuments(loggedInEmpNo);
        model.addAttribute("sentDocuments", sentDocuments);
        return "approval/main";
    }

    @GetMapping("/receive")  // 받은 문서함 페이지로 이동
    public String approvalReceive(Model model) {
        Long loggedInEmpNo = 2L; // 예시로 설정한 로그인된 사용자의 사원 번호 < 여기서 번호 바꾸면서 실험 ㄱㄱ

        // 특정 결재자가 연관된 모든 문서를 docNo 기준으로 최신순 조회
        List<Approval> approvals = approvalRepository.findByEmployeeEmpNoOrderByDocumentsDocNoDesc(loggedInEmpNo);

        // Approval 엔티티에서 DocumentsDTO로 변환
        List<DocumentsDTO> receivedDocuments = approvals.stream()
                .map(approval -> convertToDocumentsDTO(approval.getDocuments()))  // Approval 엔티티에서 Documents 엔티티로 접근
                .collect(Collectors.toList());

        log.info(receivedDocuments.toString());
        model.addAttribute("receivedDocuments", receivedDocuments);

        return "approval/receive";
    }

    @GetMapping("/draft")  // 임시 문서함 페이지로 이동
    public String approvalDraft(Model model) {
        Long loggedInEmpNo = 1L;
        List<DraftDocumentsDTO> draftDocuments = documentsService.getDraftDocuments(loggedInEmpNo);
        log.info(draftDocuments.toString());
        model.addAttribute("draftDocuments", draftDocuments);
        return "approval/draft";
    }

    @GetMapping("/adminRequest")
    public String approvalAdminRequest() {
        return "approval/adminRequest";
    }

    @GetMapping("/process")
    public String approvalProcess(Model model) {
        return "approval/process";
    }

    @GetMapping("/rejectionRead")
    public String approvalRejectionRead() {
        return "approval/rejectionRead";
    }

    // 여기서 부터 document 관련 메서드 입니다!!
    @PostMapping("/create-document")
    public ResponseEntity<Map<String, Object>> createDocument(@ModelAttribute DocumentsDTO documentsDTO) {
        Map<String, Object> response = new HashMap<>();
        // 요청 데이터 출력
        log.info("DocumentsDTO: {}", documentsDTO);

        try {
            // 문서 저장 후 문서 번호 반환
            int docNo = documentsService.saveDocument(documentsDTO);
            log.info("Saved document with docNo: {}", docNo);

            // 반환된 docNo를 DocumentsDTO에 설정
            documentsDTO.setDocNo(docNo);

            // 결재 요청 처리
            approvalService.requestApproval(documentsDTO);
            log.info("Approval process completed for docNo: {}", docNo);

            response.put("docNo", docNo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while saving document: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/draftRead/{docNo}")
    public String draftReadDocument(@PathVariable("docNo") int docNo,
                               Model model) {
        // 원래는 로그인한 사용자의 정보를 가져오는건데 임시로 emp_no가 1인 사용자 정보를 가져옴
        EmployeeDTO loggedInUser = userService.getLoggedInUserDetails();

        // 문서 정보 조회
        DocumentsDTO document = documentsService.getDocumentById(docNo);

        // 모델에 사용자 정보를 추가
        model.addAttribute("loggedInUser", loggedInUser);
        // 모델에 문서 데이터를 추가
        model.addAttribute("document", document);

        // 'draftRead.html' 뷰를 반환
        return "/approval/draftRead";
    }

    @GetMapping("/read/{docNo}")
    public String readDocument(@PathVariable("docNo") int docNo,
                               Model model) {
        // 원래는 로그인한 사용자의 정보를 가져오는건데 임시로 emp_no가 1인 사용자 정보를 가져옴
        EmployeeDTO loggedInUser = userService.getLoggedInUserDetails();

        // 문서 정보 조회
        DocumentsDTO document = documentsService.getDocumentById(docNo);

        // 모델에 사용자 정보를 추가
        model.addAttribute("loggedInUser", loggedInUser);
        // 모델에 문서 데이터를 추가
        model.addAttribute("document", document);

        // 'read.html' 뷰를 반환
        return "/approval/read";
    }

    @GetMapping("/adminRequest/{docNo}")
    public String adminRequestDocument(@PathVariable("docNo") int docNo,
                                       Model model) {
        // 원래는 로그인한 사용자의 정보를 가져오는건데 임시로 emp_no가 1인 사용자 정보를 가져옴
        EmployeeDTO loggedInUser = userService.getLoggedInUserDetails();

        // 문서 정보 조회
        DocumentsDTO document = documentsService.getDocumentById(docNo);

        // 모델에 사용자 정보를 추가
        model.addAttribute("loggedInUser", loggedInUser);
        // 모델에 문서 데이터를 추가
        model.addAttribute("document", document);

        // 'adminRequest.html' 뷰를 반환
        return "/approval/adminRequest";
    }

    @GetMapping("/rejectionRead/{docNo}")
    public String approvalRejectionDocument(@PathVariable("docNo") int docNo,
                                            @RequestParam("source") String source,
                                            Model model) {
        log.info("Source: {}", source);
        // 원래는 로그인한 사용자의 정보를 가져오는건데 임시로 emp_no가 3인 사용자 정보를 가져옴
        EmployeeDTO loggedInUser = userService.getLoggedInUserDetails();

        // 문서 정보 조회
        DocumentsDTO document = documentsService.getDocumentById(docNo);

        // 모델에 사용자 정보를 추가
        model.addAttribute("loggedInUser", loggedInUser);
        // 모델에 문서 데이터를 추가
        model.addAttribute("document", document);
        // 출처 페이지 정보 추가
        model.addAttribute("source", source);

        // 'rejectionRead.html' 뷰를 반환
        return "/approval/rejectionRead";
    }

    @GetMapping("/getForm/{category}")
    public ResponseEntity<FormDTO> getFormByCategory(@PathVariable String category) {
        log.info("Fetching form HTML for category: {}", category);

        FormDTO formDTO = formService.getFormByCategory(category);

        if (formDTO != null) {
            return ResponseEntity.ok(formDTO);
        } else {
            log.error("Form not found for category: {}", category);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/getDocumentData/{docNo}")
    public ResponseEntity<Map<String, Object>> getDocumentData(@PathVariable("docNo") int docNo) {
        // 원래는 로그인한 사용자의 정보를 가져오는건데 임시로 emp_no가 3인 사용자 정보를 가져옴
        EmployeeDTO loggedInUser = userService.getLoggedInUserDetails();

        // 문서 정보 및 결재 순서 조회, 코드 테스트를 위해 잠시 2L >> loggedInUser.getEmpNo() 다하면 다시 변경
        DocumentsDTO document = documentsService.getDocumentWithApprovalOrder(docNo, 2L);

        // 첫 번째 결재자가 승인했는지 여부 확인
        boolean isFirstApprovalApproved = approvalService.isFirstApprovalApproved(docNo);

        // 현재 결재순서인 자만 결재 승인, 결재 반려 버튼이 보이게하는 변수
        boolean isCurrentApprover = approvalService.isCurrentApprover(docNo, 2L);

        // 사용자가 해당 문서의 결재자인지 확인
        boolean isDocumentApprover = approvalService.isDocumentApprover(docNo, 2L);

        // 반환할 데이터를 맵에 추가
        Map<String, Object> response = new HashMap<>();
        response.put("document", document);
        response.put("loggedInUser", loggedInUser);
        response.put("isFirstApprovalApproved", isFirstApprovalApproved);
        response.put("isCurrentApprover", isCurrentApprover);
        response.put("rejectionReason", document.getRejectionReason());  // 반려 사유 추가
        response.put("isDocumentApprover", isDocumentApprover);          // 결재자인지 확인

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllEmployees")
    public ResponseEntity<Map<String, Object>> getAllEmployees() {
        try {
            List<EmployeeDTO> employees = userService.getAllEmployees();
            EmployeeDTO loggedInUser = userService.getLoggedInUserDetails();

            // 반환될 데이터를 맵에 추가
            Map<String, Object> response = new HashMap<>();
            response.put("employees", employees);
            response.put("loggedInUser", loggedInUser);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error occurred in getAllEmployees: ", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/getEmployees")
    public ResponseEntity<EmployeeDTO> getLoggedInUserDetails() {
        // 원래는 로그인한 사용자의 정보를 가져오는건데 임시로 emp_no가 1인 사용자 정보를 가져옴
        EmployeeDTO loggedInUser = userService.getLoggedInUserDetails();

        return ResponseEntity.ok(loggedInUser);
    }

    @PostMapping("/update-document")
    public ResponseEntity<String> updateDocument(@ModelAttribute DocumentsDTO documentsDTO) {
        try {
            documentsService.updateDocument(documentsDTO);
            return ResponseEntity.ok("Document updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update document");
        }
    }

    @DeleteMapping("/delete-document/{docNo}")
    public ResponseEntity<String> deleteDocument(@PathVariable int docNo) {
        try {
            documentsService.deleteDocument(docNo);
            return ResponseEntity.ok("Document deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete document");
        }
    }

    private DocumentsDTO convertToDocumentsDTO(Documents document) {
        DocumentsDTO documentsDTO = new DocumentsDTO();

        // Documents 엔티티의 필드를 DocumentsDTO에 복사
        documentsDTO.setDocNo(document.getDocNo());
        documentsDTO.setDocTitle(document.getDocTitle());
        documentsDTO.setDocBody(document.getDocBody());
        documentsDTO.setSenderName(document.getSenderName()); // 예시로 sender의 이름을 가져옴
        documentsDTO.setReqDate(document.getReqDate());
        documentsDTO.setCategory(document.getCategory());
        documentsDTO.setDocStatus(document.getDocStatus());
        // 추가적인 필드가 있으면 여기에 추가

        return documentsDTO;
    }

    // 결재 승인버튼을 누르면 timeStamp가 업데이트되는 메서드입니다!!
    @PostMapping("/approve/{docNo}")
    public String approveDocument(@PathVariable("docNo") int docNo, @RequestParam("timeStampHtml") String timeStampHtml) {
        // 로그인한 사용자의 정보를 가져옴
        EmployeeDTO loggedInUser = userService.getLoggedInUserDetails();

        // 결재 승인 처리 (문서 상태 변경)
        approvalService.approveDocument(docNo, 2L); // 임시로 테스트하기 위해 2L>>loggedInUser.getEmpNo()로 바꿀것

        documentsService.updateTimeStampHtml(docNo, timeStampHtml);

        // 승인 후 받은 문서함 페이지로 리다이렉트
        return "redirect:/approval/receive";
    }

    // 결재 반려를 처리 하는 컨트롤러 메서드
    @PostMapping("/rejectDocument")
    public ResponseEntity<String> rejectDocument(@RequestParam("docNo") int docNo,
                                                 @RequestParam("loggedInEmpNo") Long empNo,
                                                 @RequestParam("rejectMessage") String rejectMessage) {
        // 결재자 이름 조회
        String employeeName = employeesRepository.findFullNameByEmpNo(empNo);

        if (employeeName == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
        }

        // 문서 반려 처리
        approvalService.rejectDocument(docNo, empNo, rejectMessage);

        // 알림 생성은 일단 나중에 시간 되면 하자..
        /*String notificationMessage = String.format("%s번인 %s 결재자가 %d번 문서를 반려했습니다.", empNo, employeeName, docNo);
        notificationService.sendNotification(notificationMessage);*/

        return ResponseEntity.ok("Document rejected successfully");
    }
}