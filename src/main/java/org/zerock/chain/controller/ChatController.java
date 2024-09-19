package org.zerock.chain.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.chain.dto.ChatNotificationDTO;
import org.zerock.chain.model.ChatMessage;
import org.zerock.chain.service.ChatMessageService;
import org.zerock.chain.service.ChatUserService;

import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatUserService chatUserService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        // 메시지 저장 및 처리
        ChatNotificationDTO notificationDTO = chatMessageService.save(chatMessage);

        // 사원 정보 가져오기 및 클라이언트로 알림 전송
        chatUserService.findEmployeeByEmpNo(notificationDTO.getSenderEmpNo()).ifPresent(employeeDTO -> {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(notificationDTO.getRecipientEmpNo()), "/queue/messages", notificationDTO
            );
        });
    }

    @GetMapping("/messages/{senderEmpNo}/{recipientEmpNo}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable("senderEmpNo") Long senderEmpNo,
                                                              @PathVariable("recipientEmpNo") Long recipientEmpNo) {
        return ResponseEntity
                .ok(chatMessageService.findChatMessages(senderEmpNo, recipientEmpNo));
    }
    // 로그인 시 읽지 않은 메시지 불러오기
    @GetMapping("/messages/unread")
    public ResponseEntity<List<ChatMessage>> getUnreadMessages(@RequestParam("recipientEmpNo") Long recipientEmpNo) {
        // unread 메시지 검색 로직 구현
        List<ChatMessage> unreadMessages = chatMessageService.getUnreadMessages(recipientEmpNo);
        return ResponseEntity.ok(unreadMessages);
    }
}