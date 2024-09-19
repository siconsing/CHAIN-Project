package org.zerock.chain.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class TodoRequestDTO {

    private Long todoNo;
    private Long empNo;
    private String todoContent;
    private String todoCategory;
    private boolean todoStatus;
    private boolean todoFavorite;
    private LocalDate todoCreatedDate = LocalDate.now();



    }

