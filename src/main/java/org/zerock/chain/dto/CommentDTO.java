package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private Long commentNo; // 댓글 번호
    private Long qnaNo; // 문의글 번호
    private String commentAuthor; // 댓글 작성자
    private LocalDate commentCreatedDate; // 댓글 작성일
    private String commentName; // 댓글 제목
    private String commentContent; // 댓글 내용
    private boolean commentAnswered; // 대댓글 여부

}
