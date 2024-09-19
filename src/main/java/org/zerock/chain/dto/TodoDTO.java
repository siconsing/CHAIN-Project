package org.zerock.chain.dto;

import lombok.*;

import java.time.LocalDate;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoDTO {

    private Long todoNo;
    private Long empNo;
    private String todoContent;
    private String todoCategory;
    private boolean todoStatus;
    private boolean todoFavorite;
    private LocalDate todoCreatedDate = LocalDate.now();

}
