package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    private long notificationNo;
    private Long empNo;
    private String notificationType;
    private long referenceId;
    private String notificationMessage;
    private LocalDateTime notificationDate = LocalDateTime.now();
    private boolean isRead;
    private boolean enabled;

    // getRedirectUrl 메서드 추가
    public String getRedirectUrl() {
        switch (this.notificationType) {
            case "프로젝트":
                return "/project/detail/" + this.referenceId;
            case "공지사항":
                return "/notice/detail/" + this.referenceId;
            case "업무보고서":
                return "/report/detail/" + this.referenceId;
            default:
                return "/user/alarm";
        }
    }
}
