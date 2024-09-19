package org.zerock.chain.service;

import org.springframework.web.multipart.MultipartFile;
import org.zerock.chain.dto.ReportDTO;
import org.zerock.chain.dto.ReportRequestDTO;

import java.util.List;

public interface ReportService {
    List<ReportDTO> getAllReports();  // 전체목록 조회
    List<ReportDTO> getTemporaryReports();  // 임시보관 목록 조회
    ReportDTO getReportById(Long reportNo); // 특정 문서 조회/ 수정
    ReportDTO createReport(ReportDTO reportDTO);  // 업무보고서 등록
    void updateReport(Long reportNo, ReportRequestDTO reportRequestDTO); // 수정 등록
    String saveFile(MultipartFile file) throws Exception ; // 첨부파일 저장
    void deleteReport(Long reportNo);   // 문서 삭제 기능
}
