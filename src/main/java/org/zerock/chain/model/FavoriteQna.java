package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "fa_qna")
public class FavoriteQna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fa_qna_no")
    private Long faqNo;

    @Column(name = "fa_qna_name", nullable = false)
    private String faqName;

    @Column(name = "fa_qna_content", nullable = false, columnDefinition = "LONGTEXT")
    private String faqContent;

    @Column(name = "fa_qna_created_date", updatable = false)
    private LocalDateTime faqCreatedDate;

    @PrePersist
    protected void onCreate() {
        this.faqCreatedDate = LocalDateTime.now();
    }

    // 추가: String faqName, String faqContent를 받는 생성자
    public FavoriteQna(String faqName, String faqContent) {
        this.faqName = faqName;
        this.faqContent = faqContent;
    }
}
