package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project")
@ToString
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_no")
    private Long projectNo;

    @Column(length = 255, nullable = false, name = "project_name")
    private String projectName;

    @Column(name = "emp_no")
    private Long empNo;

    @Column(name = "project_start_date")
    private LocalDate projectStartDate;

    @Column(name = "project_end_date")
    private LocalDate projectEndDate;


    @Column(columnDefinition = "TEXT")
    private String participants;

    @Column(name = "project_favorite", nullable = false)
    private boolean projectFavorite = false;

    @Column(name = "project_progress")
    private Integer projectProgress = 0;

    @Column(name = "project_content", columnDefinition = "LONGTEXT")
    private String projectContent;

    @Column(name = "project_files")
    private String projectFile;

    @Column(name = "is_temporary")  // 임시 보관 여부
    private boolean isTemporary;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate = LocalDateTime.now();

    @Column(name = "progress_labels")
    private String progressLabels; // 진행도 라벨들을 쉼표로 구분된 문자열로 저장

    public void setProjectFavorite(boolean projectFavorite) {
        this.projectFavorite = projectFavorite;
    }

    public void setProjectProgress(Integer projectProgress) {
        this.projectProgress = projectProgress;
    }

    public void setIsTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }
}
