package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteQnaDTO {
    private Long faqNo;
    private String faqName;
    private String faqContent;
    private LocalDateTime faqCreatedDate;

}
