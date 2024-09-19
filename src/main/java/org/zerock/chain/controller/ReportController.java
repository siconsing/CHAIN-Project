package org.zerock.chain.controller;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.chain.dto.ProjectRequestDTO;
import org.zerock.chain.dto.ReportDTO;
import org.zerock.chain.service.FileService;
import org.zerock.chain.service.ReportService;
import org.zerock.chain.dto.ReportRequestDTO;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/report")
@Log4j2
public class ReportController {

    @Autowired
    private ReportService reportService;  // 보고서 서비스 계층 의존성 주입

    @Autowired
    private FileService fileService;  // 파일 서비스 계층 의존성 주입

    // 문서 전체 목록 조회
    @GetMapping("/list")
    public String listGET(Model model) {
        List<ReportDTO> reportList = reportService.getAllReports();  // 모든 보고서 조회
        reportList.sort(Comparator.comparing(ReportDTO::getReportUploadDate).reversed());  // 최신순으로 정렬
        model.addAttribute("reportList", reportList);  // 정렬된 보고서 목록을 모델에 추가
        return "report/list";  // 보고서 목록 페이지로 이동
    }

    // 문서 임시 보관 목록 조회
    @GetMapping("/temporary")
    public String temporaryGET(Model model) {
        List<ReportDTO> temporaryReports = reportService.getTemporaryReports();  // 임시 보관 문서 조회
        temporaryReports.sort(Comparator.comparing(ReportDTO::getReportUploadDate).reversed());  // 최신순으로 정렬
        model.addAttribute("temporaryReports", temporaryReports);  // 정렬된 임시 보관 문서를 모델에 추가
        return "report/temporary";  // 임시 보관 문서 목록 페이지로 이동
    }
    // 문서 개별 상세 조회
    @GetMapping("/detail/{reportNo}")
    public String detailGET(@PathVariable("reportNo") Long reportNo, ProjectRequestDTO projectRequestDTO, Model model) {
        ReportDTO reportDTO = reportService.getReportById(reportNo);  // 특정 보고서 번호로 보고서 조회
        model.addAttribute("report", reportDTO);  // 조회된 보고서를 모델에 추가
        return "report/detail";  // 보고서 상세 페이지로 이동
    }


    // 문서 수정 페이지
    @GetMapping("/modify/{reportNo}")
    public String modifyGET(@PathVariable("reportNo") long reportNo, Model model) {
        ReportDTO report = reportService.getReportById(reportNo);  // 수정할 보고서 조회
        model.addAttribute("report", report);  // 조회된 보고서를 모델에 추가
        return "report/modify";  // 보고서 수정 페이지로 이동
    }

    // 문서 수정 등록 기능
    @PostMapping("/modify/{reportNo}")
    public String modifyPOST(@PathVariable Long reportNo, @Valid ReportRequestDTO reportRequestDTO, BindingResult bindingResult, @RequestParam("isTemporary") boolean isTemporary, @RequestParam("reportFiles") MultipartFile reportFiles, @RequestParam("existingReportFiles") String existingReportFiles) throws Exception {
        reportRequestDTO.setTemporary(isTemporary);  // 임시 보관 여부 설정

        if (!reportFiles.isEmpty()) {
            // 새로운 파일이 업로드된 경우
            String fileName = fileService.saveFile(reportFiles);  // 파일 저장
            reportRequestDTO.setReportFiles(fileName);  // 파일 이름 설정
        } else {
            // 새로운 파일이 업로드되지 않은 경우 기존 파일 정보 유지
            reportRequestDTO.setReportFiles(existingReportFiles);  // 기존 파일 유지
        }

        // 보고서 수정 처리
        reportService.updateReport(reportNo, reportRequestDTO);
        return "redirect:/report/list";  // 수정 후 보고서 목록 페이지로 리다이렉트
    }

    // 새 문서 생성 조회
    @GetMapping("/register")
    public String registerGET(Model model) {
        model.addAttribute("reportDTO", new ReportDTO());  // 빈 보고서 DTO를 모델에 추가
        return "report/register";  // 보고서 등록 페이지로 이동
    }

    // 새 문서 등록 처리
    @PostMapping("/register")
    public String registerPOST(@Valid @ModelAttribute ReportDTO reportDTO, BindingResult bindingResult, Model model, @RequestParam("isTemporary") boolean isTemporary) {
        reportDTO.setIsTemporary(isTemporary);  // 임시 보관 여부 설정
        reportService.createReport(reportDTO);  // 새 보고서 등록
        return "redirect:/report/list";  // 등록 후 보고서 목록 페이지로 리다이렉트
    }

    // 문서 삭제 처리
    @DeleteMapping("/delete/{reportNo}")
    @ResponseBody
    public String deleteReport(@PathVariable Long reportNo) {
        reportService.deleteReport(reportNo);  // 보고서 삭제 처리
        return "redirect:/report/list";  // 삭제 후 보고서 목록 페이지로 리다이렉트
    }
}
