package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_no")
    private long notificationNo;

    @Column(name = "emp_no")
    private Long empNo;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "reference_id")
    private long referenceId;

    @Column(name = "notification_message")
    private String  notificationMessage;

    @Column(name = "notification_date", insertable = false, updatable = false)
    private LocalDateTime notificationDate = LocalDateTime.now();

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "enabled")
    private boolean enabled = true; // 활성화 여부

    public String getRedirectUrl() {
        switch (this.notificationType) {
            case "프로젝트":
                return "/project/detail/" + this.referenceId; // 프로젝트 관련 페이지로 이동
            case "공지사항":
                return "/notice/detail/" + this.referenceId; // 공지사항 관련 페이지로 이동
            case "업무보고서":
                return "/report/detail/" + this.referenceId; // 업무 보고서 관련 페이지로 이동
            case "부서":
            case "직급":
                return "/user/mypage"; // 부서나 직급 변경 시 마이페이지로 이동
            case "전자결재":
                return "/approval/main"; // 전자결재 알림일 경우 알림 페이지로 리다이렉트(영민 추가)
            default:
                return "/user/alarm"; // 기본적으로 알림 페이지로 이동
        }
    }

}
