package org.zerock.chain.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ReportDTO {

    private Long reportNo;

    private Long empNo;

    private String reportName;  // 제목
    private String reportCategory;  // 카테고리
    private String reportContent;   // 내용
    private String reportParticipants;  // 참여자
    private String reportAuthor;   // 작성자
    private String meetingTime; //회의시간
    private String meetingRoom; // 회의장소

    // 날짜 형식 변환 어노테이션 사용
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportUploadDate = LocalDate.now();

    private String reportFiles; // 첨부파일
    private boolean isTemporary = false;    // 임시보관여부

    public void setIsTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }
}
