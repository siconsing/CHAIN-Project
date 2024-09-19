package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartRequestDTO {

    private Long chartNo;          // 차트 번호
    private String chartName;      // 차트 이름
    private String chartContent; // 차트 내용
    private LocalDate chartStartDate; // 시작일
    private LocalDate chartEndDate;   // 종료일
    private String chartCategory;  // 카테고리
    private String chartProgress;  // 진행도
    private Boolean noticePinned;  // 완료 여부
    private Long chartAuthor;  //  작성자 (사원번호)
    private Long projectNo;        // 프로젝트 번호

    // 각 라벨을 받을 필드 추가
    private String progressLabel20;
    private String progressLabel40;
    private String progressLabel60;
    private String progressLabel80;
    private String progressLabel100;
}
