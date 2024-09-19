package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentsDTO {
    private Integer docNo;            // 문서 번호
    private Long loggedInEmpNo;       // 로그인한 사원 번호
    private String senderName;        // 결재문서 작성자
    private String senderDmpName;     // 작성자 부서
    private String senderRankName;    // 작성자 직급
    private String docTitle;          // 문서 제목
    private String docStatus;         // 문서 상태
    private String docBody;           // 문서 내용
    private String approvalLine;      // 결재선(결재자 이름) HTML
    private String timeStampHtml;     // 타임스탬프 HTML
    private String approverNoHtml;    // 결재자 순번 HTML
    private MultipartFile file;       // 첨부 파일
    private String filePath;          // 파일 경로
    private LocalDate reqDate;        // 요청, 저장일
    private String category;          // 양식 종류
    private String approversJson;     // 결재자 목록을 JSON 형식으로 저장
    private String referencesJson;    // 참조자 번호를 JSON 형식으로 저장
    private int approvalOrder;        // 결재 순서 - 다른 엔티티 부분
    private String approvalStatus;    // 결재 상태 - 다른 엔티티 부분
    private String rejectionReason;   // 반려 사유 - 다른 엔티티 부분
    private int virtualNo;            // 사용자에게 표시할 가상의 번호 - 엔티티엔 있지만 DB에는 저장 안되는 부분
    private String withdraw;          // 철회 여부를 위한 플래그 - 엔티티엔 있지만 DB에는 저장 안되는 부분
}