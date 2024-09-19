package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Documents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_no")
    private Integer docNo;              // 문서 번호

    @Column(name = "logged_in_emp_no", nullable = false)
    private Long loggedInEmpNo;         // 로그인한 사원 번호 = 결재를 요청하는 사원의 번호

    @Column(name = "sender_name", nullable = false)
    private String senderName;          // 문서 작성자의 이름

    @Column(name = "sender_dmp_name")
    private String senderDmpName;       // 작성자 부서명

    @Column(name = "sender_rank_name")
    private String senderRankName;      // 작성자 직급명

    @Column(name = "doc_title", nullable = false)
    private String docTitle;            // 문서 제목

    @Column(name = "doc_status")
    private String docStatus;           // 문서 상태

    @Column(name = "req_date")
    private LocalDate reqDate;          // 작성일 / 저장일

    @Column(name = "category")
    private String category;            // 양식 종류

    @Column(name = "doc_body", columnDefinition = "longtext")
    private String docBody;             // 문서 내용

    @Column(name = "approval_line", columnDefinition = "longtext")
    private String approvalLine;        // 결재선 Div 태그 HTML 부분

    @Column(name = "time_stamp_html", columnDefinition = "longtext")
    private String timeStampHtml;       // 타임스탬프 Div 태그 HTML 부분

    @Column(name = "approver_no_html", columnDefinition = "longtext")
    private String approverNoHtml;      // 결재자 순서 번호 Div 태그 HTML 부분

    @Column(name = "file_path")
    private String filePath;            // 파일 경로

    @Column(name = "approver_json", columnDefinition = "text")
    private String approversJson;        // 결재자 정보(결재자 사원 번호, 결재 순번)

    @Column(name = "reference_json", columnDefinition = "text")
    private String referencesJson;        // 결재자 정보(결재자 사원 번호, 결재 순번)

    @Transient
    private int virtualNo;              // 사용자에게 표시할 가상의 번호

    @Transient
    private String withdraw;  // 철회 여부를 위한 플래그 (엔티티에 저장되지 않음)

    @Override
    public String toString() {
        return "DocumentsEntity{" +
                "docNo=" + docNo +
                "loggedInEmpNo=" + loggedInEmpNo +
                ", docTitle='" + docTitle + '\'' +
                ", docStatus='" + docStatus + '\'' +
                ", reqDate=" + reqDate +
                ", category='" + category + '\'' +
                ", docBody='" + docBody + '\'' +
                ", approvalLine='" + approvalLine + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
