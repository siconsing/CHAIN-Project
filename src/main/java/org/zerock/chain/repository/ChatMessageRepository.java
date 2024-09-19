package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    // 특정 채팅방의 메시지 목록 조회
    List<ChatMessage> findByChatNo(String chatNo);

    // 특정 수신자의 읽지 않은 메시지 목록 조회
    List<ChatMessage> findByRecipientEmpNoAndIsReadFalse(Long recipientEmpNo);

    // 특정 채팅방의 모든 메시지를 읽음 처리
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatNo = :chatNo AND m.senderEmpNo = :senderEmpNo AND m.recipientEmpNo = :recipientEmpNo")
    void markMessagesAsRead(
            @Param("chatNo") String chatNo,
            @Param("senderEmpNo") Long senderEmpNo,
            @Param("recipientEmpNo") Long recipientEmpNo
    );

    // 특정 채팅방의 모든 메시지 삭제
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.chatNo = :chatNo")
    void deleteByChatNo(@Param("chatNo") String chatNo);
}
