package org.zerock.chain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequestDTO {

    private Long reportNo;

    private String reportName;

    private Long empNo;

    private String reportCategory;
    private String reportContent;
    private String reportAuthor;
    private LocalDate reportUploadDate = LocalDate.now();
    private String reportFiles;
    private boolean isTemporary;
    private String reportParticipants;
    private String meetingTime;
    private String meetingRoom;

}
