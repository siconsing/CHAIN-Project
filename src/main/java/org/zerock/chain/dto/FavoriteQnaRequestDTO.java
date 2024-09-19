package org.zerock.chain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteQnaRequestDTO {
    private Long faqNo;
    private String faqName;
    private String faqContent;
}
