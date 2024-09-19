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
public class ChatNotificationDTO {
    private Long id;
    private Long senderEmpNo;
    private Long recipientEmpNo;
    private String chatContent;
    private LocalDateTime chatSentTime = LocalDateTime.now();
}
