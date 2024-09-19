package org.zerock.chain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.service.ChatMessageService;
import org.zerock.chain.service.ChatRoomService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatRoomController {
    // 클라이언트에서 들어오는 요청을 받아 서비스 메서드 호출
    private final ChatMessageService chatMessageService;
    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/markAsRead")
    public ResponseEntity<Void> markMessagesAsRead(@RequestBody Map<String, Long> request) {
        Long senderEmpNo = request.get("senderEmpNo");
        Long recipientEmpNo = request.get("recipientEmpNo");
        chatMessageService.markMessagesAsRead(senderEmpNo, recipientEmpNo);

        return ResponseEntity.ok().build();
    }

    // 채팅방 나가기
    @PostMapping("/exit")
    public ResponseEntity<Void> exitChatRoom(@RequestBody Map<String, Long> request) {
        Long senderEmpNo = request.get("senderEmpNo");
        Long recipientEmpNo = request.get("recipientEmpNo");
        chatRoomService.exitChatRoom(senderEmpNo, recipientEmpNo);
        return ResponseEntity.ok().build();
    }

    // 방 존재 여부 확인 엔드포인트
    @GetMapping("/checkRoomExistence")
    public ResponseEntity<Boolean> checkRoomExistence(
            @RequestParam("senderEmpNo") Long senderEmpNo,
            @RequestParam("recipientEmpNo") Long recipientEmpNo) {
        boolean roomExists = chatRoomService.checkRoomExistence(senderEmpNo, recipientEmpNo);
        return ResponseEntity.ok(roomExists);
    }
}
