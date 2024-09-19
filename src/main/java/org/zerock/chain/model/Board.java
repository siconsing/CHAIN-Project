package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "board")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_no")  // 경조사 번호
    private Long boardNo;

    @Column(name = "board_category")  // 카테고리
    private String boardCategory;

    @Column(name = "board_name")  // 제목
    private String boardName;

    @Column(name = "board_content", columnDefinition = "LONGTEXT")  // 내용
    private String boardContent;

    @Column(name = "board_location")  // 지도 위치 (API 연동 예정)
    private String boardLocation;

    @Column(name = "board_files")  // 첨부파일
    private String boardFiles;

    @Column(name = "board_author")  // 작성자
    private String boardAuthor;

    @Column(name = "board_upload_date")  // 작성일
    private LocalDate boardUploadDate = LocalDate.now();
}
