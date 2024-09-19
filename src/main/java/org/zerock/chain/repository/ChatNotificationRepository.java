package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.ChatNotification;

@Repository
public interface ChatNotificationRepository extends JpaRepository<ChatNotification, Long> {
    // 특정 채팅방의 모든 알림 삭제
    @Modifying
    @Query("DELETE FROM ChatNotification n WHERE n.chatMessage.chatNo = :chatNo")
    void deleteByChatNo(@Param("chatNo") String chatNo);
}
