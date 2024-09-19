package org.zerock.chain.service;

import org.zerock.chain.model.SystemNotification;

import java.util.List;

public interface SystemNotificationService {
    void saveSystemNotification(SystemNotification systemNotification);
    List<SystemNotification> getAllSystemNotifications(Long empNo); // 사원 번호로 모든 알림 조회
    void deleteAllSystemNotifications();
    void deleteSystemNotification(Long systemNo);
    void markAsReadSystem(Long systemNo);  // 알림을 읽음으로 표시
    SystemNotification getSystemNotificationById(Long systemNo); // 개별 조회
    void deleteReadSystemNotification(Long empNo); // 특정 사원의 읽은 알림을 모두 삭제
}
