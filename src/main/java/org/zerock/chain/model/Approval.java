package org.zerock.chain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "approvals")
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_no")
    private Long approvalNo;  // 결재번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_no", referencedColumnName = "doc_no")
    @JsonIgnore
    private Documents documents;    // 문서 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_no", referencedColumnName = "emp_no")
    @JsonBackReference
    private Employee employee;      // 결재자 사원 번호

    @Column(name = "approval_date", nullable = true)
    private LocalDateTime approvalDate;  // 결재일 (최종 결재일)

    @Column(name = "rejection_reason")
    private String rejectionReason;  // 결재 반려 사유

    @Column(name = "approval_order")
    private Integer approvalOrder;  // 결재 순서

    @Column(name = "approval_status")
    private String approvalStatus;  // 결재 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_emp_no", referencedColumnName = "emp_no")
    @JsonBackReference
    private Employee refEmployee;  // 참조자 사원 번호
}
