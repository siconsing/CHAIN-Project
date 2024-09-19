package org.zerock.chain.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class ProjectRequestDTO {
    private Long projectNo;
    private Long empNo;
    private String projectName;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private String participants;
    private String projectContent;
    private String projectFiles;
    private boolean projectFavorite;
    private int projectProgress;
    private boolean isTemporary; // 임시 보관
    private LocalDateTime uploadDate = LocalDateTime.now();
    private String progressLabels; // 진행도 라벨들을 쉼표로 구분된 문자열로 저장



    public void setIsTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }
    public void setProjectFavorite(boolean projectFavorite) {
        this.projectFavorite = projectFavorite;
    }
    public void setProjectProgress(Integer projectProgress) {
        this.projectProgress = projectProgress;
    }

    // 각 라벨을 받을 필드 추가
    private String progressLabel20;
    private String progressLabel40;
    private String progressLabel60;
    private String progressLabel80;
    private String progressLabel100;
}
