package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Notification;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEmpNoAndNotificationType(Long empNo, String notificationType); // 알림 타입별 조회

    List<Notification> findByEmpNo(Long empNo); // 사원 번호로 모든 알림 조회

    void deleteByEmpNo(Long empNo);  // 사원 번호로 모든 알림 삭제

    Optional<Notification> findById(Long notificationNo); // 알림 번호로 개별 조회

    // 알림 타입 및 사원 번호로 enabled 상태 업데이트
    @Modifying
    @Query("UPDATE Notification n SET n.enabled = :enabled WHERE n.empNo = :empNo AND n.notificationType = :notificationType")
    void updateEnabledByEmpNoAndType(@Param("empNo") Long empNo, @Param("notificationType") String notificationType, @Param("enabled") Boolean enabled);

    // 새로운 메서드: 사원 번호로 읽은(isRead = true) 알림 삭제
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.empNo = :empNo AND n.isRead = true")
    void deleteByEmpNoAndIsRead(@Param("empNo") Long empNo); // 사원 번호로 읽은 알림 삭제
}
