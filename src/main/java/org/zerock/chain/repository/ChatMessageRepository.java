package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom_ChatRoomNo(Long chatRoomNo);

    // 채팅 메시지 관련 쿼리 메소드
}