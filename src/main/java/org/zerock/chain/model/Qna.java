package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "qna")
@ToString
public class Qna {

    /** 질문 테이블 (질문번호, 제목, 카테고리, 작성자, 내용, 첨부파일, 작성일 )
     * 답글 테이블 (질문번호, 답글 번호, 답글 제목, 답글 내용, 작성일)*/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_no")
    private Long qnaNo; // 질문아이디,번호

    // Qna와 Comment 간의 관계 설정
    @OneToMany(mappedBy = "commentQna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "qna_name")
    private String qnaName; // 제목

    @Column(name = "qna_author")
    private String qnaAuthor; // 작성자

    @Column(name = "qna_content", columnDefinition = "LONGTEXT")
    private String qnaContent; // 내용

    @Column(name = "qna_category")
    private String qnaCategory; // 카테고리

    @Column(name = "qna_status")
    private boolean qnaStatus = false; //  답변여부

    @Column(name = "qna_files")
    private String qnaFiles;    //첨부파일

    @Column(name = "todo_upload_date", updatable = false)
    private LocalDate qnaUploadDate;    // 작성일

    @PrePersist // 자동 날짜 등록
    protected void onCreate() {
        this.qnaUploadDate = LocalDate.now();
    }

    public void setQnaStatus(boolean qnaStatus) {
        this.qnaStatus = qnaStatus;
    }

    // 댓글 추가 메서드
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setCommentQna(this);
    }

    // 댓글 제거 메서드
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setCommentQna(null);
    }

}
