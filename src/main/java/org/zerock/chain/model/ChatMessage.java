package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_no")
    private String chatNo; // 방

    @Column(name = "sender_emp_no")
    private Long senderEmpNo; // 발신자의 emp_no

    @Column(name = "recipient_emp_no")
    private Long recipientEmpNo; // 수신자의 emp_no

    @ManyToOne
    @JoinColumn(name = "chat_room_no", referencedColumnName = "id")
    private ChatRoom chatRoom; // 방번호

    @Column(name = "chat_content")
    private String chatContent; // 내용

    @Column(name = "chat_sent_time")
    private LocalDateTime chatSentTime = LocalDateTime.now(); // 보낸 시간

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;  // 메시지 확인 여부
}
