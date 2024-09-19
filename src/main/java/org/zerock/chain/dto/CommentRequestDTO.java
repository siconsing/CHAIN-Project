package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {

    private Long qnaNo; // 문의글 번호
    private String commentAuthor; // 댓글 작성자
    private String commentName; // 댓글 제목
    private String commentContent; // 댓글 내용
    private boolean commentAnswered; // 대댓글 여부



}
