package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_notification")
@Data
@AllArgsConstructor
@NoArgsConstructor
// 시스템 관리자
public class SystemNotification {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "system_no")
    private Long systemNo;

    @Column(name = "emp_no")
    private Long empNo;

    @Column(name = "system_category")
    private String systemCategory;

    @Column(name = "system_title")
    private String systemTitle;

    @Column(name = "system_content", columnDefinition = "LONGTEXT")
    private String systemContent;

    @Column(name = "system_upload_date")
    private LocalDateTime systemUploadDate;

    @Column(name = "is_read")
    private boolean isRead = false;

}
