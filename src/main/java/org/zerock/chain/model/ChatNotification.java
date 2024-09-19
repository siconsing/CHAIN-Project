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
@Table(name = "chat_notification")
public class ChatNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "chat_message_id", referencedColumnName = "id")
    private ChatMessage chatMessage;  // chat_message 테이블의 id를 참조

    @Column(name = "sender_emp_no")
    private Long senderEmpNo; // 발신자의 emp_no

    @Column(name = "recipient_emp_no")
    private Long recipientEmpNo; // 수신자의 emp_no

    @Column(name = "chat_content")
    private String chatContent;

    @Column(name = "chat_sent_time")
    private LocalDateTime chatSentTime = LocalDateTime.now();
}
