package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDTO {
    private Long approvalNo;         // 결재 고유 번호
    private Long docNo;              // 문서 번호
    private Long empNo;              // 결재자 사원 번호
    private Long refEmpNo;           // 참조자 사원 번호
    private LocalDateTime approvalDate;  // 결재 일시
    private String rejectionReason;  // 반려 사유
    private int approvalOrder;       // 결재 순서
    private String approvalStatus;   // 결재 상태 ("대기", "승인", "반려")
}
