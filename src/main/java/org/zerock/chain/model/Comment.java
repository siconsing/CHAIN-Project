package org.zerock.chain.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_no")
    private Long commentNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_no", nullable = false)
    private Qna commentQna;

    @Column(name = "comment_author")
    private String commentAuthor;

    @Column(name = "comment_created_date")
    private LocalDate commentCreatedDate = LocalDate.now();

    @Column(name = "comment_name")
    private String commentName;

    @Column(name = "comment_content", columnDefinition = "LONGTEXT")
    private String commentContent;

    @Column(name = "comment_answered")
    private boolean commentAnswered = false;


}
