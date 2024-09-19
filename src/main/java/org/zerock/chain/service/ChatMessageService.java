package org.zerock.chain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.ChatNotificationDTO;
import org.zerock.chain.model.ChatMessage;
import org.zerock.chain.model.ChatNotification;
import org.zerock.chain.model.Employee;
import org.zerock.chain.model.ChatRoom;
import org.zerock.chain.repository.ChatMessageRepository;
import org.zerock.chain.repository.ChatNotificationRepository;
import org.zerock.chain.repository.ChatRoomRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository messageRepository;
    private final ChatNotificationRepository notificationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    // 메시지 저장 및 최신 시간 업데이트
    @Transactional
    public ChatNotificationDTO save(ChatMessage chatMessage) {
        var chatRoom = chatRoomService.getChatRoom(chatMessage.getSenderEmpNo(), chatMessage.getRecipientEmpNo(), true)
                .orElseThrow(() -> new IllegalStateException("채팅방을 생성할 수 없습니다."));

        chatMessage.setChatNo(chatRoom.getChatNo());
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setRead(false);
        chatMessage.setChatSentTime(LocalDateTime.now());

        try {
            ChatMessage savedMessage = messageRepository.save(chatMessage);

            // 동일한 chat_no을 가지고 isDeleted가 false인 방에 대해 최신 시간을 업데이트
            List<ChatRoom> activeChatRooms = chatRoomRepository.findByChatNoAndIsDeletedFalse(chatRoom.getChatNo());
            if (!activeChatRooms.isEmpty()) {
                for (ChatRoom activeChatRoom : activeChatRooms) {
                    activeChatRoom.setLatestTime(savedMessage.getChatSentTime()); // 최신 시간 설정
                    chatRoomRepository.save(activeChatRoom); // 저장
                    log.info("ChatRoom updated with latestTime: {}", activeChatRoom.getLatestTime());
                }
            }

            // ChatNotification 생성 및 저장
            ChatNotification notification = ChatNotification.builder()
                    .chatMessage(savedMessage)
                    .senderEmpNo(savedMessage.getSenderEmpNo())
                    .recipientEmpNo(savedMessage.getRecipientEmpNo())
                    .chatContent(savedMessage.getChatContent())
                    .chatSentTime(savedMessage.getChatSentTime())
                    .build();
            notificationRepository.save(notification);

            // unread_count 증가
            chatRoom.setUnreadCount(chatRoom.getUnreadCount() + 1);
            chatRoomRepository.save(chatRoom);

            // 메시지와 최신 시간 정보 반환
            return ChatNotificationDTO.builder()
                    .senderEmpNo(savedMessage.getSenderEmpNo())
                    .recipientEmpNo(savedMessage.getRecipientEmpNo())
                    .chatContent(savedMessage.getChatContent())
                    .chatSentTime(savedMessage.getChatSentTime())
                    .build();

        } catch (Exception e) {
            log.error("Error saving message", e);
            throw new RuntimeException("메시지 저장 중 오류가 발생했습니다.");
        }
    }

    // 현재 사용자의 대화 중인 사용자 목록 반환
    public List<Employee> findActiveChatUsers(Long empNo) {
        return chatRoomRepository.findActiveChatUsersByEmpNo(empNo);
    }

    // 채팅방 재생성 시 최신 메시지 반환 및 이후 채팅만 저장
    public List<ChatMessage> findChatMessages(Long senderEmpNo, Long recipientEmpNo) {
        var chatRoom = chatRoomService.getChatRoom(senderEmpNo, recipientEmpNo, true);
        return chatRoom
                .filter(room -> !chatRoomService.isChatRoomDeleted(senderEmpNo, recipientEmpNo))
                .map(room -> messageRepository.findByChatNo(room.getChatNo()))
                .orElse(new ArrayList<>());
    }

    // 로그인 시 읽지 않은 메시지 불러오기
    public List<ChatMessage> getUnreadMessages(Long recipientEmpNo) {
        return messageRepository.findByRecipientEmpNoAndIsReadFalse(recipientEmpNo);
    }

    // 메시지 읽음 처리 & 메시지 수 초기화
    @Transactional
    public void markMessagesAsRead(Long senderEmpNo, Long recipientEmpNo) {
        var chatRoom = chatRoomService.getChatRoom(senderEmpNo, recipientEmpNo, false);

        chatRoom.ifPresent(room -> {
            // 메시지 읽음 처리 (DB)
            messageRepository.markMessagesAsRead(room.getChatNo(), senderEmpNo, recipientEmpNo);
            room.setUnreadCount(0);  // unread_count 초기화
            chatRoomRepository.save(room);  // DB에 저장

            // A 사원에게 읽음 처리 완료 메시지 전송
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(senderEmpNo), "/queue/read", true);

            // B 사원에게도 읽음 처리 완료 메시지 전송
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipientEmpNo), "/queue/read", true);
        });
    }
}
