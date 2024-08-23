package org.zerock.chain.ksh.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.chain.ksh.service.ChatMessageService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatRoomController {
    // 클라이언트에서 들어오는 요청을 받아 서비스 메서드 호출
    private final ChatMessageService chatMessageService;

    @PostMapping("/markAsRead")
    public ResponseEntity<Void> markMessagesAsRead(@RequestBody Map<String, Long> request) {
        try {
            Long senderEmpNo = request.get("senderEmpNo");
            Long recipientEmpNo = request.get("recipientEmpNo");
            // ChatRoomService에서 처리하는 대신, ChatMessageService로 이동
            chatMessageService.markMessagesAsRead(senderEmpNo, recipientEmpNo);
            return ResponseEntity.ok().build();

        } catch (Exception e){}

        return null;
    }
}