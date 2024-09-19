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
public class ChatRoomDTO {
    private Long id;
    private String chatNo;
    private Long senderEmpNo;
    private Long recipientEmpNo;
    private int unreadCount;
    private boolean isDeleted;
    private LocalDateTime latestTime = LocalDateTime.now();
}
