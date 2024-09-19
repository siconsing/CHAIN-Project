package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDTO {

    private Long chartNo;          // 차트 번호
    private String chartName;      // 차트 이름
    private String chartContent; // 차트 내용
    private LocalDate chartStartDate; // 시작일
    private LocalDate chartEndDate;   // 종료일
    private String chartCategory;  // 카테고리
    private String chartProgress;  // 진행도
    private Boolean noticePinned;  // 완료 여부
    private LocalDate chartUploadDate; // 생성일
    private Long chartAuthor;  //  작성자 (사원번호)
    private String progressLabels; // 진행도 라벨들을 쉼표로 구분된 문자열로 저장
}
