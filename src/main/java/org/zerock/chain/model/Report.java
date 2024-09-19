package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportNo;

    @Column(name = "emp_no")
    private Long empNo;

    @Column(name = "report_name", length = 255, nullable = false)
    private String reportName;

    @Column(name = "report_category", length = 100)
    private String reportCategory;

    @Column(name = "report_content", columnDefinition = "LONGTEXT")
    private String reportContent;

    @Column(name = "report_author")
    private String reportAuthor;

    @Column(name = "report_upload_date")
    private LocalDate reportUploadDate = LocalDate.now();

    @Column(name = "report_files")
    private String reportFiles;

    @Column(name = "is_temporary")
    private boolean isTemporary;

    @Column(name = "report_participants")
    private String reportParticipants;

    @Column(name = "meeting_time")
    private String meetingTime;

    @Column(name = "meeting_room", columnDefinition = "TEXT")
    private String meetingRoom;


    public void setIsTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }
}