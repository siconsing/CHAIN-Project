package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDTO {

    private Long noticeNo;
    private Integer empNo;
    private String noticeName;
    private String noticeContent;
    private Boolean noticePinned;
    private LocalDate noticeCreatedDate;
    private String noticeFiles;
    private String noticeAuthor;
    private LocalDate noticePinnedDate;
}
