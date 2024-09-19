package org.zerock.chain.dto;

import java.time.LocalDateTime;

public class NotificationDTO {

    private long notificationNo;
    private Long empNo;

    private String notificationType;
    private long referenceId;
    private String  notificationMessage;
    private LocalDateTime notificationDate;
    private boolean isRead;
    private boolean enabled;

    // getRedirectUrl 메서드 추가 (선택 사항)
    public String getRedirectUrl() {
        switch (this.notificationType) {
            case "프로젝트":
                return "/project/detail/" + this.referenceId; // 프로젝트 관련 페이지로 이동
            case "공지사항":
                return "/notice/detail/" + this.referenceId; // 공지사항 관련 페이지로 이동
            case "업무보고서":
                return "/report/detail/" + this.referenceId; // 업무 보고서 관련 페이지로 이동
            default:
                return "/user/alarm"; // 기본적으로 알림 페이지로 이동
        }
    }
}
