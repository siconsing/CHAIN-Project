package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chart")
@ToString
public class Chart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chart_no")
    private Long chartNo; // 차트 번호

    @Column(name = "chart_name")
    private String chartName; // 차트 이름

    @Column(name = "chart_content", columnDefinition = "LONGTEXT")
    private String chartContent; // 차트 내용

    @Column(name = "chart_start_date")
    private LocalDate chartStartDate;  // 시작일

    @Column(name = "chart_end_date")
    private LocalDate chartEndDate;// 종료일

    @Column(name = "chart_category")
    private String chartCategory;// 카테고리

    @Column(name = "chart_progress")
    private String chartProgress ;   // 진행도

    @Column(name = "notice_pinned")
    private Boolean noticePinned = false;   // 완료 여부

    @Column(name = "chart_upload_date")
    private LocalDate chartUploadDate= LocalDate.now();    // 생성일

    @Column(name = "chart_author")
    private Long chartAuthor;  //  작성자 (사원번호)

    @Column(name = "progress_labels")
    private String progressLabels; // 진행도 라벨들을 쉼표로 구분된 문자열로 저장

}
