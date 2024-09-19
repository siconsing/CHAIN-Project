package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.SystemNotification;

import java.util.List;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {

    List<SystemNotification> findByEmpNo(Long empNo); // 사원 번호로 모든 알림 조회

    // 새로운 메서드: 사원 번호로 읽은(isRead = true) 알림 삭제
    @Modifying
    @Query("DELETE FROM SystemNotification n WHERE n.empNo = :empNo AND n.isRead = true")
    void deleteByEmpNoAndIsRead(@Param("empNo") Long empNo); // 사원 번호로 읽은 알림 삭제
}