package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form")
public class Form {

    @Id
    private String category;  // 양식의 카테고리

    @Lob
    @Column(name = "form_html", columnDefinition = "longtext")
    private String formHtml;  // 양식의 HTML을 저장
}
