package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "notice")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_no")
    private Long noticeNo;

    @Column(name = "emp_no")
    private Integer empNo = 1; // Integer로 변경하고 기본값 설정

    @Column(name = "notice_name")
    private String noticeName;

    @Column(name = "notice_content", columnDefinition = "LONGTEXT")
    private String noticeContent;

    @Column(name = "notice_pinned")
    private Boolean noticePinned = false;

    @Column(name = "notice_created_date")
    private LocalDate noticeCreatedDate = LocalDate.now();

    @Column(name = "notice_files")
    private String noticeFiles;

    @Column(name = "notice_author")
    private String noticeAuthor;

    @Column(name = "notice_pinned_date")
    private LocalDate noticePinnedDate;
}