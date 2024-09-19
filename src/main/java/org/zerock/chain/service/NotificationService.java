package org.zerock.chain.service;

import org.zerock.chain.model.Notification;

import java.util.List;

public interface NotificationService {

    List<Notification> getNotificationsByType(Long empNo, String notificationType); // 알림 타입별 조회
    List<Notification> getAllNotifications(Long empNo); // 사원 번호로 모든 알림 조회
    void deleteAllNotifications(Long empNo);  // 사원 번호로 모든 알림 삭제
    void deleteNotification(Long notificationNo);   // 알림 번호로 개별 삭제
    void markAsRead(Long notificationNo);  // 알림을 읽음으로 표시
    Notification getNotificationById(Long notificationNo);
    void updateNotificationSettingByType(Long empNo, String notificationType, Boolean enabled);  // 알림 온오프
    void deleteReadNotifications(Long empNo); // 특정 사원의 읽은 알림을 모두 삭제함


    // 전자결재 알림을 생성하는 메서드 추가 (영민)
    void createApprovalNotification(int docNo, String docTitle, String senderName, String docStatus, String withdraw);

    // 부서 및 직급 변경 알림을 생성하는 메서드
    void createDepartmentAndRankChangeNotification(Long empNo, String oldDepartment, String newDepartment, String oldRank, String newRank);



}
