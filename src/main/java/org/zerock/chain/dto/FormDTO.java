package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDTO {

    private String category; // 양식 종류
    private String formHtml; // 각 양식 HTML 구조가 저장 되는 곳
}
