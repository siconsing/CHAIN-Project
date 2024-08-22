package org.zerock.chain.ksh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.ksh.model.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByChatNo(String chatNo);

    // 읽지 않은 메시지를 조회하기 위한 메서드
    List<ChatMessage> findByRecipientEmpNoAndIsReadFalse(Long recipientEmpNo);

    // 메시지 읽음 처리하는 쿼리 추가 (Custom Query 필요)
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatNo = :chatNo")
    void markMessagesAsRead(@Param("chatNo") String chatNo);

}
