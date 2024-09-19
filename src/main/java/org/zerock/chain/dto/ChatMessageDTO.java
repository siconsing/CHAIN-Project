package org.zerock.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String chatNo;
    private Long senderEmpNo;
    private Long recipientEmpNo;
    private Long chatRoomNo;
    private String chatContent; // 메시지 내용
    private LocalDateTime chatSentTime = LocalDateTime.now(); // 채팅 보낸 시간
    private boolean isRead;
}
