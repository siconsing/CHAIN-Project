package org.zerock.chain.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.model.Employee;
import org.zerock.chain.model.EmployeeLeave;
import org.zerock.chain.repository.ApprovalRepository;
import org.zerock.chain.repository.EmployeeRepository;
import org.zerock.chain.service.EmployeeLeaveService;
import org.zerock.chain.dto.DocumentsDTO;
import org.zerock.chain.model.Approval;
import org.zerock.chain.model.Documents;
import org.zerock.chain.service.DocumentsService;
import org.zerock.chain.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/attendance")
@Log4j2
public class CompanyController {

    private final UserService userService;
    private final EmployeeLeaveService employeeLeaveService;
    private final DocumentsService documentsService;
    private final ApprovalRepository approvalRepository;
    private final EmployeeRepository employeeRepository;

    public CompanyController(UserService userService, EmployeeLeaveService employeeLeaveService, DocumentsService documentsService, ApprovalRepository approvalRepository, EmployeeRepository employeeRepository) {
        this.userService = userService;
        this.employeeLeaveService = employeeLeaveService;
        this.documentsService = documentsService;
        this.approvalRepository = approvalRepository;
        this.employeeRepository = employeeRepository;
    }

    // 각 사용자의 연차 신청서 내역
    @GetMapping("/emp_leave/{empNo}")
    public String empLeave(@PathVariable("empNo") Long empNo, Model model) {
        List<DocumentsDTO> sentDocuments = documentsService.getSentDocuments(empNo);

        List<DocumentsDTO> leaveDocuments = sentDocuments.stream()
                .filter(document -> "연차신청서".equals(document.getCategory()))
                .collect(Collectors.toList());

        int virtualNo = leaveDocuments.size();
        for (DocumentsDTO document : leaveDocuments) {
            document.setVirtualNo(virtualNo--);
            log.info("leaveDocument: {}, virtualNo: {}", document.getDocNo(), document.getVirtualNo());
        }

        String fullName = employeeRepository.findFullNameByEmpNo(empNo);

        model.addAttribute("fullName", fullName);
        model.addAttribute("leaveDocuments", leaveDocuments);
        return "/admin/attendance/emp_leave";
    }

    // 연차 신청서 페이지
    @GetMapping("/leave")
    public String atLeave(Model model) {
        Long loggedInEmpNo = userService.getLoggedInUserEmpNo();

        List<Approval> approvals = approvalRepository.findByEmployeeOrRefEmployee(loggedInEmpNo);

        List<DocumentsDTO> receivedDocuments = approvals.stream()
                .map(approval -> convertToDocumentsDTO(approval.getDocuments()))
                .collect(Collectors.toList());

        int virtualNo = receivedDocuments.size();
        for (DocumentsDTO document : receivedDocuments) {
            document.setVirtualNo(virtualNo--);
            log.info("leaveDocument: {}, leaveVirtualNo: {}", document.getDocNo(), document.getVirtualNo());
        }

        List<Employee> employeeLeaveList = employeeRepository.findAll();
        model.addAttribute("employeeLeaveList", employeeLeaveList);
        model.addAttribute("receivedDocuments", receivedDocuments);

        return "admin/attendance/leave";
    }

    private DocumentsDTO convertToDocumentsDTO(Documents document) {
        DocumentsDTO documentsDTO = new DocumentsDTO();
        documentsDTO.setDocNo(document.getDocNo());
        documentsDTO.setDocTitle(document.getDocTitle());
        documentsDTO.setDocBody(document.getDocBody());
        documentsDTO.setSenderName(document.getSenderName());
        documentsDTO.setReqDate(document.getReqDate());
        documentsDTO.setCategory(document.getCategory());
        documentsDTO.setDocStatus(document.getDocStatus());
        return documentsDTO;
    }

    // 사용자 연차 정보 프론트로 주는 메서드
    @GetMapping("/emp_leave/{empNo}/leaveInfo")
    @ResponseBody
    public Map<String, Object> getEmployeeLeaveInfo(@PathVariable("empNo") Long empNo) {
        EmployeeLeave employeeLeave = employeeLeaveService.getEmployeeLeaveByEmpNo(empNo);
        String fullName = employeeRepository.findFullNameByEmpNo(empNo);

        Map<String, Object> leaveInfo = new HashMap<>();
        leaveInfo.put("usedLeaveDays", employeeLeave.getUsedLeaveDays());  // 사용 연차
        leaveInfo.put("unusedLeaveDays", employeeLeave.getUnusedLeaveDays());  // 미사용 연차
        leaveInfo.put("fullName", fullName); // 사용자 이름 추가

        return leaveInfo;
    }

    // 모달창에서 업데이트 된 사용 연차 일수 변경에 따른 연차 정보 반환하는 메서드
    @PostMapping("/emp_leave/{empNo}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> updateEmployeeLeave(@PathVariable("empNo") Long empNo, @RequestBody Map<String, Integer> requestData) {
        int usedLeaveDays = requestData.get("usedLeaveDays");

        // 서비스 레이어에서 데이터 업데이트 처리 및 결과 반환
        Map<String, Integer> updatedLeaveData = employeeLeaveService.updateUsedLeaveDays(empNo, usedLeaveDays);

        if (updatedLeaveData != null) {
            return ResponseEntity.ok(updatedLeaveData);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
