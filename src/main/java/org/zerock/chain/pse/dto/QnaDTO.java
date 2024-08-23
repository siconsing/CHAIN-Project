package org.zerock.chain.pse.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QnaDTO {

    private Long qnaNo;
    private String qnaName;
    private String qnaAuthor;
    private String qnaContent;
    private String qnaCategory;
    private boolean qnaStatus; //  답변여부
    private MultipartFile qnaFiles;
    private LocalDateTime qnaUploadDate;

}